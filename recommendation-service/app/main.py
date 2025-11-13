import os
from fastapi import FastAPI
from app.controllers import recommendation_controller
from app.controllers import chat_controller 
from dotenv import load_dotenv
from fastapi.middleware.cors import CORSMiddleware

# Load environment variables
load_dotenv()

app = FastAPI(
    title="DVFashion Recommendation Service",
    description="API for product recommendations and AI chat.",
    version="1.0.0"
)

# Subscribe the recommendation controller
app.include_router(
    recommendation_controller.router, 
    prefix="/api/v1", 
    tags=["Recommendations"]
)

app.include_router(
    chat_controller.router,
    prefix="/api/v1",
    tags=["Chatbot"]
)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "app.main:app",
        host=os.getenv("APP_HOST"),
        port=int(os.getenv("APP_PORT")),
        reload=True
    )