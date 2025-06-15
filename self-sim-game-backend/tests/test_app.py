import pytest

###############################################################################
# 테스트 헬퍼 – demo 토큰 확보 (/token)                                      #
###############################################################################

auth_payload = {"username": "demo", "password": "demo"}

async def _ensure_user_and_get_token(client):
    """demo 계정이 없으면 등록 후 /token 으로 토큰 발급."""
    res = await client.post("/token", json=auth_payload)
    if res.status_code == 200:
        return res.json()["access_token"]

    await client.post("/register", json=auth_payload)
    res = await client.post("/token", json=auth_payload)
    assert res.status_code == 200, "Token 발급 실패"
    return res.json()["access_token"]

###############################################################################
# Auth                                                                       #
###############################################################################

@pytest.mark.asyncio
async def test_token_success(client):
    token = await _ensure_user_and_get_token(client)
    assert token and isinstance(token, str)

@pytest.mark.asyncio
async def test_token_fail(client):
    bad = {**auth_payload, "password": "wrong"}
    res = await client.post("/token", json=bad)
    assert res.status_code in {400, 401}

###############################################################################
# Story                                                                      #
###############################################################################

@pytest.mark.asyncio
async def test_get_story_success(client):
    token = await _ensure_user_and_get_token(client)
    headers = {"Authorization": f"Bearer {token}"}
    res = await client.get("/story/1", headers=headers)
    assert res.status_code in {200, 404}

@pytest.mark.asyncio
async def test_get_story_404(client):
    token = await _ensure_user_and_get_token(client)
    headers = {"Authorization": f"Bearer {token}"}
    res = await client.get("/story/9999", headers=headers)
    assert res.status_code == 404

###############################################################################
# Log                                                                        #
###############################################################################

@pytest.mark.asyncio
async def test_save_choice_log(client):
    token = await _ensure_user_and_get_token(client)
    headers = {"Authorization": f"Bearer {token}"}

    payload = {
        "scene_id": 1,
        "timestamp": "2025-06-15T00:00:00Z",
        "log": [{"scene": "1", "choice": "go_left"}],
    }
    res = await client.post("/log", json=payload, headers=headers)
    assert res.status_code in {200, 201}
