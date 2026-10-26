ace Form — Face-Shape Analyzer & Style Guide
Face Form is a small full-stack application that turns a face photo into an AI-generated face-shape estimate and a set of optional style ideas. A browser interface is served by a Spring Boot application; prediction requests are forwarded to a separate, token-protected Python service. The Java application also serves the hairstyle and eyewear guides.

This repository is useful as an example of a thin API gateway coordinating browser, Java, and Python components while keeping the user-facing API separate from the model service.

What the application does
Accepts JPEG, PNG, and WebP photos up to 10 MB, with drag-and-drop, preview, and upload validation in the browser.
Returns a predicted face-shape label, model confidence, per-class scores, and model version from the inference service.
Provides guides for heart, oval, oblong, square, and round face shapes.
Suggests hairstyles and eyewear, with hairstyle filters for length, texture, and maintenance level.
Returns structured API errors for invalid uploads and downstream-service failures.
Architecture

Prediction flow
The browser checks the selected file's MIME type and size, then previews it locally.
POST /api/v1/face-shape/predict receives the image as multipart field file. The gateway checks the size and JPEG, PNG, or WebP signature.
The gateway forwards the image to the configured Python service as field image, attaching the internal X-Service-Token header.
The Python service validates and decodes the image, extracts 478 MediaPipe face landmarks, normalizes them relative to the eyes, and supplies the resulting 956 X/Y features to a scikit-learn classifier.
The browser displays the predicted shape, confidence, score breakdown, and the matching style guide.
The Python service loads model artifacts from ml-service/models. If either artifact is missing, it downloads the missing file from the configured, pinned Hugging Face model revision during startup.

Technology
Area	Technologies
Gateway and web server	Java 17, Spring Boot 3.5, Spring Web, Bean Validation
Browser interface	HTML, CSS, and vanilla JavaScript, served from Spring Boot static resources
Inference service	Python, FastAPI, Uvicorn
Face processing and prediction	MediaPipe Face Mesh, Pillow, OpenCV, NumPy, scikit-learn
Build and tests	Maven Wrapper, Spring Boot Test, JUnit
Run locally
You need Java 17, Python 3.11, and internet access on the first ML-service startup if its model files are not already present. Start the services in separate terminals from the directory containing this README.

1. Start the Python inference service
In PowerShell:

cd ml-service
py -3.11 -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install -r requirements.txt
$env:FACE_SERVICE_TOKEN = "replace-with-the-same-private-token"
python -m uvicorn "ml-service:app" --app-dir . --host 127.0.0.1 --port 8001
The token is required at service startup. The first startup may take longer while the model artifacts are downloaded and loaded.

2. Start the Spring Boot application
In a second PowerShell terminal, from the directory containing this README:

$env:FACE_SERVICE_BASE_URL = "http://127.0.0.1:8001"
$env:FACE_SERVICE_SERVICE_TOKEN = "replace-with-the-same-private-token"
.\mvnw.cmd spring-boot:run
FACE_SERVICE_TOKEN configures the Python service; FACE_SERVICE_SERVICE_TOKEN configures the gateway. Their values must match. The gateway's downstream URL and timeouts can also be configured with FACE_SERVICE_BASE_URL, FACE_SERVICE_CONNECT_TIMEOUT, and FACE_SERVICE_READ_TIMEOUT. Keep credentials out of source control and use managed secrets in deployed environments.

Open http://localhost:8080/ to use the application. The static frontend and API are served from the same Spring Boot origin.

To run the Java test suite:

.\mvnw.cmd test
The current Java test suite includes an application-context startup test.

API
POST /api/v1/face-shape/predict
Submit a multipart form with an image in the file field.

curl -X POST http://localhost:8080/api/v1/face-shape/predict \
  -H "Accept: application/json" \
  -F "file=@portrait.jpg"
Successful response (200 OK):

{
  "face_shape": "oval",
  "confidence": 0.84,
  "scores": [
    { "label": "oval", "probability": 0.84 },
    { "label": "round", "probability": 0.09 }
  ],
  "model_version": "Rob4343/FaceShapeAI@<revision>"
}
The sample scores above are illustrative. The gateway maps common downstream errors to API statuses, including 400 for invalid images, 413 for files over the limit, 422 when no face is detected, and 503 when the model service is unavailable. Errors use an error code and a human-readable message.

GET /api/v1/style-guides/{shape}
Returns the guide for a supported shape: heart, oval, oblong, square, or round.

curl http://localhost:8080/api/v1/style-guides/oval \
  -H "Accept: application/json"
The response includes a summary, visual clues, hairstyle options, glasses options, uncertainty factors, styling tips, and a disclaimer. Guide data is defined in the Java application; it does not require a database.

Internal ML service
The gateway calls POST /v1/face-shape/predict on the Python service, sending the image in multipart field image and the shared token in X-Service-Token. The service also exposes GET /health, which reports whether the model is loaded.

Project layout
.
├── src/main/java/face_app/gateway/
│   ├── client/       # HTTP client for the Python inference service
│   ├── config/       # Typed downstream-service configuration
│   ├── controller/   # Public prediction and style-guide endpoints
│   ├── dto/          # Prediction, guide, and error response types
│   ├── exception/    # Consistent API error handling
│   └── service/      # Upload validation and guide data
├── src/main/resources/
│   ├── static/       # Browser application (index.html)
│   └── application.yaml
├── ml-service/
│   ├── ml-service.py # FastAPI app and inference pipeline
│   ├── requirements.txt
│   └── models/       # Model and label-encoder artifacts
├── pom.xml
└── mvnw / mvnw.cmd
Responsible use and privacy
Face-shape output is a model estimate, not a measurement, diagnosis, or guarantee of accuracy. Camera angle, lighting, framing, hair, and image quality can affect results. Style suggestions are optional ideas; personal preference and comfort should take priority.

The application has no user-account or image-persistence layer. Images are sent from the browser to the gateway and then to the inference service for processing. Obtain permission before uploading someone else's photo, and review privacy and retention requirements before deploying the application for real users.
