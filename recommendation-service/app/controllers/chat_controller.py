from fastapi import APIRouter
from pydantic import BaseModel
from typing import Any, Dict

from app.services.chat_ai_service import process_message

router = APIRouter()

class ChatRequest(BaseModel):
    message: str

class ChatResponse(BaseModel):
    intent: str
    reply: str
    products: list[Any]

@router.post("/chat", response_model=ChatResponse)
async def chat_with_ai(payload: ChatRequest) -> Dict:
    """
    Endpoint chat chính
    """
    result = await process_message(payload.message)
    return result
