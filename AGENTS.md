# Workspace Rules & Guidelines

## General Rules for Agents & Developers

1. **No AI-Positive or AI-Characteristic Icons**:
   - Strictly forbidden to use any AI-positive, AI-characteristic, or generative AI-associated icons, emojis, or symbols anywhere in the codebase (UI components, buttons, badges, notifications, labels, or documentation).
   - Prohibited examples: hourglass (`⏳`, `⌛`), sparkles (`✨`), magic wand (`🪄`), robot (`🤖`), brain (`🧠`), lightbulb (`💡`) when used as AI decoration.
   - Always use standard, minimalist functional SVG icons and plain, professional, clean text labels.

2. **Domain Isolation & Modular Design**:
   - Follow clean code, Hexagonal Architecture for backend, and Domain-Driven Design / Modular structure for frontend.

3. **Verification**:
   - Ensure builds (`npm run build`, `./mvnw test`) pass cleanly before task completion.
