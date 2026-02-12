# Implementation Plan - Literature Archive

## Overview
This document outlines the current implementation status and immediate next steps for the Literature Archive project, focusing on the User, Chat, and Knowledge services.

## 1. User Service (Authentication)
**Status**: Core JWT infrastructure is implemented.
- [x] **JWT Service**: `JwtTokenService` implemented with RSA-2048 key pair generation.
- [x] **JWK Exposure**: Method to export JWK Set JSON is ready for Gateway/Resource Server configuration.
- [ ] **Next Steps**:
  - Integrate with Spring Security to secure API endpoints.
  - Verify JWK Set endpoint exposure for other services to consume.

## 2. Chat Service (Real-time Communication)
**Status**: Netty server pipeline and authentication handshake are in progress.
- [x] **Netty Pipeline**: configured in `NettyServerInitializer` with:
  - `LengthFieldBasedFrameDecoder` (Sticky/half packet handling)
  - `ChatProtocolDecoder` / `ChatProtocolEncoder`
  - `ChatCryptoCodec` (Encryption layer)
  - `IdleStateHandler` (Heartbeat)
  - `AuthHandler` (JWT Authentication)
- [x] **Authentication**: `AuthHandler` validates JWT tokens and registers user sessions.
- [ ] **Next Steps**:
  - **Message Handling**: Finalize `ChatMessageHandler` to handle chat messages after auth.
  - **Encryption**: Verify `ChatCryptoCodec` works with the frontend crypto implementation.
  - **Offline Messages**: Ensure `OfflineMessageService` correctly stores and retrieves messages.
  - **Session Routing**: Test `SessionRouteService` with Redis for cluster support.

## 3. Knowledge Service (Semantic Search)
**Status**: Embedding engine and Vector Store integration are established.
- [x] **Embedding Service**: `EmbeddingService` implemented supporting:
  - Dynamic model provider switching (Ollama/OpenAI).
  - Milvus Vector Store integration.
  - Text-to-Vector methods (`embed`, `store`, `search`).
- [ ] **Next Steps**:
  - **Content Ingestion**: Implement logic to slice `Books` and `Chapters` into `TextSegment`s.
  - **Search API**: Expose REST endpoints for semantic search queries.
  - **Hybrid Search**: Combine vector search with keyword search (Elasticsearch).

## 4. Immediate Action Items
1.  **Chat Service**: Debug/Test the Netty connection flow with a real client to verify the JWT auth handshake.
2.  **Knowledge Service**: Write a unit test or simple controller to verify Milvus connectivity and embedding generation.
3.  **Documentation**: Keep `backend_prd.md` updated with any API changes.
