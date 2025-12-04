# Usability Testing Report
## Authentication System - Java Application

**Date:** December 1st, 2025  
**Application:** auth-screen v1.0.0  
**Test Type:** Heuristic Evaluation, Cognitive walkthrough
**Evaluator:** María Hernández López

---

## 1. Introduction
The purpose of this usability test is to evaluate how effectively and efficiently users can interact with the application’s main features. This session focuses on identifying potential usability issues, understanding user behavior, and gathering feedback that will help improve the overall user experience. The findings presented in this report summarize participant performance, observed difficulties, and opportunities for design enhancements that can support a more intuitive and user-friendly product.

---

## 2. Heuristic evaluation
The heuristic evaluation for the authentication screen will be performed based on Jakob Nielsen's 10 usability heuristics, a set of guidelines based on understanding of human behavior, psychology and information processing.

### 2.1 Visibility status evaluation
**Purpose**
Users should receive immediate and clear feedback after every action so they know whether the system is processing, finished, or encountered an error. This reduces uncertainty, prevents confusion, and builds trust by making the system feel responsive and predictable.

**Observations**
- Interface does not show any status indicators such as correct email format or input validation.
- Messages should be clearer whether email address exists in database or not
- Missing loading indicator after pressing login

### 2.2 Match between system and real world
**Purpose**
The system should follow real-world logic and terminology (not technical jargon) so users can immediately understand what to do without learning new or unnatural terms. This makes the system feel intuitive and approachable.

**Observations**
- Labels are clear to understand for spanish speakers
- Phrasing matches user expectations
- Tone is consistent

### 2.3 User control and freedom
**Purpose**
People make mistakes, explore, or click things accidentally — users must feel in control, able to undo, redo, or return to a safe point without frustration. This reduces anxiety and encourages exploration.

**Observations**
- Users can access to recover their password easily

### 2.4 Consistency and standards
**Purpose**
When layouts, icons, wording, and behaviors are consistent — both internally and with common platform standards — users do not need to relearn patterns. This reduces cognitive load and creates a seamless experience.

**Observations**
- Button styles are consistent with each other
- Labels are alligned
- Typical login conventions are followed
- There's a clear contrast between background and text
- Text is sized enough to be easy to read

### 2.5 Error prevention
**Purpose**
Rather than only displaying error messages after problems happen, the system should guide users, validate inputs, and design workflows that reduce errors proactively. Preventing an error is always easier and less frustrating than recovering from one.

**Observations**
- There are no guidance to prevent errors such as incorrect email format or empty fields

### 2.6 Recognition rather than recall
**Purpose**
Interfaces should present options, hints, or contextual information so users don’t have to remember instructions, input formats, or previous steps. Recognition is faster and less prone to mistakes than recall.

**Observations**
- Interface does not require to remember information
- Fields could have a placeholder to guide users visually on what is expected from their part

### 2.7 Flexibility and efficiency
**Purpose**
Beginners need guidance, while experienced users want shortcuts, accelerators, and faster ways to perform tasks. A flexible design improves usability for all user types and boosts productivity.

**Observations**
- No shortcuts for advanced users like enter key to log in
- `Tab` key works for jumping from email to password field
- Basic or new users will understand everything easily

### 2.8 Aesthetic
**Purpose**
Excess or irrelevant content distracts users, slows comprehension, and increases cognitive load. A minimalist design improves clarity, keeps users focused on the essential tasks, and enhances overall user satisfaction.

**Observations**
- Clean, minimalist interface with dark background and blue buttons.
- Good spacing and no visual clutter
- High contrast between text and background

### 2.9 Help users recognize and recover from errors
**Purpose**
Good error messages highlight the problem clearly, avoid blaming the user, and give actionable instructions for recovery. This reduces frustration and enables users to continue smoothly.

**Observations**
- No visible nor clear error messages on wrong login attempts
- No password visibility toggle

### 2.10 Help and documentation
**Purpose**
Even though systems should be usable without documentation, some guidance may still be necessary. Help should be easy to find, concise, task-focused, and written in plain language to support the user effectively.

**Observations**
- No help elements visible
- Loging screens often include links to support or FAQ pages.

---

## 3. Cognitive walkthrough
The purpose of a cognitive walkthrough is to evaluate how easily a first-time user can complete a task based entirely on the interface itself, without instructions or prior experience.
This method focuses on learnability and simulates a user’s step-by-step thought process when attempting to complete a task.

**Task:** Log in to the system using the provided authentication interface.

### 3.1 User profile
- First-time user
- Basic computer literacy
- No technical knowledge required
- May include users with visual impairments or color blindness
- Uses typical authentication systems (email + password)

### 3.2 Observations
#### 3.2.1 Does the user understand the purpose of the screen?
**Observation**
The title "Iniciar Sesión" at the top of the form clearly indicates that this is a login screen.

**Result**
Likely successful
No issues identified.

#### 3.2.2 Can the user find where to enter their email?
**Observation**
The email field is clearly labeled “Correo electrónico” with a placeholder.
Visual grouping is strong and consistent.

**Result**
Likely successful
No issues identified.

#### 3.2.3 Can the user correctly enter an email?
**Observation**
Nothing in the interface prevents the user from typing. However, there is no indication of required format (example: user@example.com).

No inline validation until after submission.

**Result**
Possibly successful but with room for improvement
Issue: Lack of formatting guidance may lead to user error.

#### 3.2.4 Can the user locate the password field?
**Observation**
The password field is visually identical to the email field, properly labeled as “Clave”.

**Result**
Likely successful
No issues identified.

#### 3.2.5 Can the user enter a password correctly?
**Observation**
Password input masks characters, which is standard.
No visibility toggle (“mostrar clave”) is present.

**Result**
Likely successful but may frustrate some users
Issue: No password visibility toggle can hinder accessibility and error correction.

#### 3.2.6 Can the user identify how to submit the login form?
**Observation**
Primary button “Entrar” is clearly differentiated with contrast and styling.
Its purpose is immediately recognizable.

**Result**
Likely successful
No issues identified.

#### 3.2.7 Does the user know what happens after pressing “Entrar”?
**Observation**
User knows what to expect, however the site does not show any feedback mechanism (spinner, disabled button, etc).

**Result**
Potential issue
Issue: The system may not clearly indicate that login attempt is processing.

#### 3.2.8 If there is an error, is the feedback clear?
**Observation**
The authentication screen does not show clear error messages (e.g., “Email no válido”, “Contraseña incorrecta”).

**Result**
Uncertain
Issue: Lack of visible error messages may confuse users.