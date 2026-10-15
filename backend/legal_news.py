"""Fetch legal news from official Indian government/legal sources."""

import feedparser
import requests
from bs4 import BeautifulSoup


def get_pib_news():
    feed_url = "https://www.pib.gov.in/RssMain.aspx?ModId=6&Lang=1&Regid=1"
    feed = feedparser.parse(feed_url)
    news = []

    for entry in feed.entries[:10]:
        title = entry.get("title", "").strip()
        link = entry.get("link", "").strip()
        published = entry.get("published", "").strip()

        if not title or not link:
            continue

        news.append({
            "title": title,
            "summary": "Official Government of India update.",
            "category": "Government",
            "date": published,
            "source": "Press Information Bureau",
            "url": link,
            "image": "",
        })

    return news


def get_supreme_court_updates():
    url = "https://www.sci.gov.in/latest-updates/"
    headers = {"User-Agent": "Mozilla/5.0 VidhiSetu/1.0"}

    try:
        response = requests.get(url, headers=headers, timeout=15)
        response.raise_for_status()
        soup = BeautifulSoup(response.text, "html.parser")
        news = []

        for link in soup.find_all("a"):
            title = link.get_text(" ", strip=True)
            href = link.get("href")

            if not title or not href:
                continue

            lower_title = title.lower()
            keywords = [
                "judgment", "court", "notice", "circular", "order", "legal"
            ]

            if any(word in lower_title for word in keywords):
                if href.startswith("/"):
                    href = "https://www.sci.gov.in" + href

                news.append({
                    "title": title,
                    "summary": "Latest official update from the Supreme Court of India.",
                    "category": "Supreme Court",
                    "date": "",
                    "source": "Supreme Court of India",
                    "url": href,
                    "image": "",
                })

            if len(news) >= 10:
                break

        return news

    except requests.RequestException:
        # Keep the API usable if this source is temporarily unavailable.
        return []


def get_legal_news():
    news = []

    try:
        news.extend(get_pib_news())
    except Exception:
        # Do not fail all results when the RSS source is unavailable.
        pass

    try:
        news.extend(get_supreme_court_updates())
    except Exception:
        pass

    return news[:20]
