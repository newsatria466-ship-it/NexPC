from fastapi import Header, HTTPException, Query, status
from agent.app.config import get_live_token, is_auth_enabled
from typing import Optional

def verify_token(
    x_nexpc_token: Optional[str] = Header(None),
    authorization: Optional[str] = Header(None),
    token: Optional[str] = Query(None)
):
    if not is_auth_enabled():
        return True

    provided_token = None
    if x_nexpc_token:
        provided_token = x_nexpc_token
    elif authorization and authorization.startswith("Bearer "):
        provided_token = authorization.split("Bearer ")[1].strip()
    elif token:
        provided_token = token

    clean_provided = (provided_token or "").strip().upper()
    clean_expected = (get_live_token() or "").strip().upper()

    if not clean_provided or clean_provided != clean_expected:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or missing authentication token"
        )
    return True
