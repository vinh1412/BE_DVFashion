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

# CORS configuration
origins = [
    "http://localhost:5173",  
    "http://127.0.0.1:5173",
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"], 
    allow_headers=["*"],
)

@app.get("/")
def read_root():
    return {"message": "Welcome to DVFashion Recommendation API"}

# Gồm các router từ các controller
app.include_router(recommendation_controller.router, prefix="/api/v1", tags=["Recommendation"])
app.include_router(chat_controller.router, prefix="/api/v1", tags=["Chat"])

@app.on_event("startup")
async def startup_event():
    # This will trigger the startup event in recommendation_controller
    await recommendation_controller.startup_event()

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