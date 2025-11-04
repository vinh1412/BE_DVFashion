import pandas as pd
from app.repositories.model_repository import save_model_evaluation
from app.repositories.interaction_repository import fetch_user_interactions
from app.services.recommendation_service import recommendation_engine
from typing import List, Dict

def evaluate_model(model_name: str, k: int = 10):
    """Đánh giá mô hình gợi ý hiện tại theo các chỉ số Precision@K, Recall@K, MAP@K"""

    print(f"Evaluating model '{model_name}' with K={k} ...")
    interactions_df = fetch_user_interactions()
    print("Interactions:", len(interactions_df))
    print("Unique users:", interactions_df['user_id'].nunique())
    print("Unique products:", interactions_df['product_id'].nunique())

    if interactions_df.empty:
        print("No user interactions available for evaluation.")
        return None

    user_ids = interactions_df['user_id'].unique()
    precision_scores, recall_scores, map_scores = [], [], []

    for user_id in user_ids:
        # Danh sách sản phẩm user đã tương tác (ground truth)
        true_items = set(interactions_df[interactions_df['user_id'] == user_id]['product_id'])
        if not true_items:
            continue

        # Gợi ý top-K sản phẩm
        recs = recommendation_engine.get_hybrid_recommendations(user_id=user_id, num_recommendations=k)
        print(f"User {user_id} recommended items: {[r['product_id'] for r in recs]}")
        recommended_items = [r['product_id'] for r in recs]

        if not recommended_items:
            continue

        # Precision@K
        correct = len(set(recommended_items) & true_items)
        precision = correct / k
        # Recall@K
        recall = correct / len(true_items)
        # MAP@K
        precisions = []
        hits = 0
        for idx, pid in enumerate(recommended_items, 1):
            if pid in true_items:
                hits += 1
                precisions.append(hits / idx)
        avg_precision = sum(precisions) / len(true_items) if true_items else 0

        precision_scores.append(precision)
        recall_scores.append(recall)
        map_scores.append(avg_precision)

    avg_precision = round(sum(precision_scores) / len(precision_scores), 4)
    avg_recall = round(sum(recall_scores) / len(recall_scores), 4)
    avg_map = round(sum(map_scores) / len(map_scores), 4)

    print(f"Model {model_name} evaluation complete:")
    print(f"Precision@{k}={avg_precision}, Recall@{k}={avg_recall}, MAP@{k}={avg_map}")

    return {
        "model_name": model_name,
        "content_weight": recommendation_engine.content_weight,
        "collaborative_weight": recommendation_engine.collaborative_weight,
        "precision_at_10": avg_precision,
        "recall_at_10": avg_recall,
        "map_at_10": avg_map
    }


def evaluate_model_split(model_name: str, k: int = 10) -> Dict:
    """Đánh giá mô hình theo chuẩn train/test 80-20"""

    print(f"Evaluating model '{model_name}' (train/test 80–20, K={k}) ...")
    df = fetch_user_interactions()

    if df.empty:
        print("No user interactions found.")
        return None

    user_ids = df['user_id'].unique()
    precision_scores, recall_scores, map_scores = [], [], []

    for user_id in user_ids:
        user_data = df[df['user_id'] == user_id]
        if len(user_data) < 3:
            # Bỏ qua user có ít hơn 3 interactions
            continue

        # Chia train/test 80/20
        shuffled = user_data.sample(frac=1, random_state=42)
        split_index = int(0.8 * len(shuffled))
        train_data = shuffled.iloc[:split_index]
        test_data = shuffled.iloc[split_index:]

        train_items = set(train_data['product_id'])
        test_items = set(test_data['product_id'])

        if not test_items:
            continue

        # Giả lập user train interactions (mô hình đã được train trước)
        recs = recommendation_engine.get_hybrid_recommendations(
            user_id=user_id,
            num_recommendations=k,
            exclude_items=train_items
        )

        recommended_items = [r['product_id'] for r in recs]
        if not recommended_items:
            continue

        # Precision@K
        correct = len(set(recommended_items) & test_items)
        precision = correct / k
        recall = correct / len(test_items)
        # MAP@K
        precisions = []
        hits = 0
        for idx, pid in enumerate(recommended_items, 1):
            if pid in test_items:
                hits += 1
                precisions.append(hits / idx)
        avg_precision = sum(precisions) / len(test_items) if precisions else 0

        precision_scores.append(precision)
        recall_scores.append(recall)
        map_scores.append(avg_precision)

    if not precision_scores:
        print("Not enough users for evaluation.")
        return {
            "model_name": model_name,
            "precision@10": 0.0,
            "recall@10": 0.0,
            "map@10": 0.0
        }

    avg_precision = round(sum(precision_scores) / len(precision_scores), 4)
    avg_recall = round(sum(recall_scores) / len(recall_scores), 4)
    avg_map = round(sum(map_scores) / len(map_scores), 4)

    print("\nEvaluation Summary:")
    print(f"Users evaluated: {len(precision_scores)}")
    print(f"Average Precision@{k}: {avg_precision:.4f}")
    print(f"Average Recall@{k}: {avg_recall:.4f}")
    print(f"Average MAP@{k}: {avg_map:.4f}")
    print("──────────────────────────────────────────────")

    return {
        "model_name": model_name,
        "content_weight": recommendation_engine.content_weight,
        "collaborative_weight": recommendation_engine.collaborative_weight,
        "precision_at_10": avg_precision,
        "recall_at_10": avg_recall,
        "map_at_10": avg_map
    }