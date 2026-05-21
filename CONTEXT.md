# Knowledge Base System

A knowledge base maintenance and AI Q&A system built for the JavaEE course project. Users maintain a curated knowledge collection and query it through an AI-powered Q&A interface.

## Language

**Knowledge Base**:
The collection of interview and technical knowledge entries stored in the system.
_Avoid_: Question bank, quiz bank.

**Knowledge Atom**:
A single entry in the knowledge base, containing a subject title, category, difficulty level, tags, core principles content, and optional pitfalls notes.
_Avoid_: Question, document, record.

**Active Atom**:
A knowledge atom with ACTIVE status, visible in search and available for AI Q&A retrieval.
_Avoid_: Published atom, available atom.

**Archived Atom**:
A knowledge atom with ARCHIVED status, hidden from search and AI Q&A. Can be restored back to ACTIVE.
_Avoid_: Deleted atom, removed atom.

**Knowledge Q&A**:
The AI-powered question-answering feature that retrieves relevant knowledge atoms via keyword search, then generates an answer using an LLM grounded in the retrieved content.
_Avoid_: Chat, AI chat, chatbot.

**Bulk Import**:
Uploading a JSON file containing multiple knowledge atoms and inserting them into the database after validation.
_Avoid_: Batch publish, file upload.

**Single Entry**:
Adding one knowledge atom through a form with individual fields.
_Avoid_: Manual add, form entry.

**Category**:
A predefined classification label for knowledge atoms, configured in application settings.
_Avoid_: Topic, tag group.

## Relationships

- The **Knowledge Base** contains many **Knowledge Atoms**.
- A **Knowledge Atom** is either **Active** or **Archived**.
- **Active Atoms** participate in **Knowledge Q&A** retrieval.
- **Archived Atoms** are excluded from search and Q&A but can be restored.
- **Bulk Import** validates and inserts multiple atoms at once.
- **Single Entry** creates one atom through a form interface.
- Each **Knowledge Atom** belongs to one **Category**.

## Example dialogue

> **User:** "If I archive an atom, can I still find it in search?"
> **Domain expert:** "No. An **Archived Atom** is excluded from search and AI Q&A. You can restore it to **Active** status at any time."

## Flagged ambiguities

- "Delete" was used to mean both soft-delete and hard-delete. Resolved: use **Archive** for soft-delete (status change) and avoid hard-delete entirely.
- "Publish" implied a Qdrant sync step. Resolved: atoms are created as **Active** directly; no separate publish workflow exists.
- "RAG" implied vector semantic search. Resolved: **Knowledge Q&A** uses MySQL LIKE-based keyword retrieval, not vector search.
