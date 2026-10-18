import unittest
from unittest.mock import patch

from app import app


class CivilianQuestionEndpointTests(unittest.TestCase):
    def setUp(self):
        app.config["TESTING"] = True
        self.client = app.test_client()

    def payload(self, language="en", **overrides):
        data = {
            "language": language,
            "initialProblem": "I have a problem with my employer",
            "timeline": "",
            "peopleInvolved": "",
            "documents": "",
            "actionsTaken": "",
            "desiredOutcome": "",
            "questionCount": 0,
        }
        data.update(overrides)
        return data

    def test_requires_initial_problem(self):
        response = self.client.post(
            "/api/civilian/question",
            json={"language": "en"},
        )
        self.assertEqual(response.status_code, 400)

    def test_rejects_unsupported_language(self):
        response = self.client.post(
            "/api/civilian/question",
            json=self.payload(language="fr"),
        )
        self.assertEqual(response.status_code, 400)

    @patch("app.ollama_client.chat", side_effect=RuntimeError("Ollama is offline"))
    def test_english_fallback_is_explicit(self, _mock_chat):
        response = self.client.post(
            "/api/civilian/question",
            json=self.payload(language="en"),
        )
        body = response.get_json()
        self.assertEqual(response.status_code, 200)
        self.assertTrue(body["fallback"])
        self.assertFalse(body["complete"])
        self.assertIn("When did", body["question"])

    @patch("app.ollama_client.chat", side_effect=RuntimeError("Ollama is offline"))
    def test_hindi_fallback_uses_devanagari(self, _mock_chat):
        response = self.client.post(
            "/api/civilian/question",
            json=self.payload(language="hi"),
        )
        body = response.get_json()
        self.assertEqual(response.status_code, 200)
        self.assertTrue(body["fallback"])
        self.assertRegex(body["question"], r"[\u0900-\u097F]")

    @patch("app.ollama_client.chat", side_effect=RuntimeError("Ollama is offline"))
    def test_telugu_fallback_uses_telugu_script(self, _mock_chat):
        response = self.client.post(
            "/api/civilian/question",
            json=self.payload(language="te"),
        )
        body = response.get_json()
        self.assertEqual(response.status_code, 200)
        self.assertTrue(body["fallback"])
        self.assertRegex(body["question"], r"[\u0C00-\u0C7F]")

    def test_completes_after_five_answers(self):
        response = self.client.post(
            "/api/civilian/question",
            json=self.payload(questionCount=5),
        )
        self.assertEqual(response.status_code, 200)
        self.assertTrue(response.get_json()["complete"])


if __name__ == "__main__":
    unittest.main()


class LegalRagProxyTests(unittest.TestCase):
    def setUp(self):
        app.config["TESTING"] = True
        self.client = app.test_client()

    def payload(self, **overrides):
        data = {
            "language": "en",
            "role": "civilian",
            "initialProblem": "My landlord changed the lock",
            "timeline": "Yesterday",
            "peopleInvolved": "My landlord and me",
            "documents": "Rental agreement",
            "actionsTaken": "",
            "desiredOutcome": "Recover access",
            "additionalDetails": "",
            "top_k": 3,
        }
        data.update(overrides)
        return data

    def test_requires_valid_language_and_role(self):
        response = self.client.post(
            "/api/legal/answer", json=self.payload(language="fr")
        )
        self.assertEqual(response.status_code, 400)
        response = self.client.post(
            "/api/legal/answer", json=self.payload(role="admin")
        )
        self.assertEqual(response.status_code, 400)

    def test_reports_unconfigured_rag_without_fabricating_answer(self):
        with patch("app.RAG_API_URL", ""):
            response = self.client.post("/api/legal/answer", json=self.payload())
        self.assertEqual(response.status_code, 503)
        body = response.get_json()
        self.assertEqual(body["rag_status"], "not_configured")
        self.assertNotIn("answer", body)

    @patch("app.requests.post")
    def test_forwards_case_facts_and_normalises_citations(self, mock_post):
        with patch("app.RAG_API_URL", "https://rag.example"):
            mock_post.return_value.status_code = 200
            mock_post.return_value.json.return_value = {
                "status": "SUCCESS",
                "query": "some query",
                "simplified_explanation": "Grounded explanation",
                "key_takeaways": ["Section 1"],
                "disclaimer": "Educational information only.",
                "citations": ["Example Act, Section 1"],
                "supporting_passages": [{
                    "chunk_id": "chunk-1",
                    "act": "Example Act",
                    "section_number": 1,
                    "section_title": "Example",
                    "page_start": 3,
                    "page_end": 3,
                    "source": "data/act.pdf",
                    "text": "Verbatim provision",
                }],
            }
            response = self.client.post("/api/legal/answer", json=self.payload())

        self.assertEqual(response.status_code, 200)
        body = response.get_json()
        self.assertEqual(body["answer"], "Grounded explanation")
        self.assertEqual(body["citations"], ["Example Act, Section 1"])
        self.assertEqual(body["passages"][0]["text"], "Verbatim provision")
        self.assertIn("Initial situation", mock_post.call_args.kwargs["json"]["query"])
        self.assertTrue(mock_post.call_args.args[0].endswith("/api/v1/civilian/ask"))

    @patch("app.requests.post")
    def test_upstream_timeout_is_reported_as_gateway_timeout(self, mock_post):
        import requests
        mock_post.side_effect = requests.Timeout("timed out")
        with patch("app.RAG_API_URL", "https://rag.example"):
            response = self.client.post("/api/legal/answer", json=self.payload())
        self.assertEqual(response.status_code, 504)
        self.assertEqual(response.get_json()["rag_status"], "timeout")


