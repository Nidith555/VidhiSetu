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
