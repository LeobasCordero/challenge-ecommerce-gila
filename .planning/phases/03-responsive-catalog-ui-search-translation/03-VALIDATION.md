---
phase: 3
slug: responsive-catalog-ui-search-translation
status: approved
nyquist_compliant: true
wave_0_complete: true
created: 2026-07-11
---

# Phase 3 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | Angular CLI Test Runner (Jasmine + Karma) |
| **Config file** | frontend/angular.json |
| **Quick run command** | `cd frontend && npx ng test --watch=false` |
| **Full suite command** | `cd frontend && npx ng test --watch=false` |
| **Linting check** | `cd frontend && npx ng lint && npx stylelint "**/*.scss"` |

---

## Sampling Rate

- **After every task commit:** Run `cd frontend && npx ng build`
- **After every plan wave:** Run `cd frontend && npx ng test --watch=false && npx ng lint`
- **Before `/gsd-verify-work`:** Full build, test suite, and linting checks must be green

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|-----------------|-----------|-------------------|-------------|--------|
| 3-01-01 | 03 | 1 | R18 | Standalone routing initialized | build | `npm run build` | ✅ | ✅ green |
| 3-01-02 | 03 | 1 | R24 | Custom Emerald Light Sass compiled | build | `npm run build` | ✅ | ✅ green |
| 3-01-03 | 03 | 1 | R1 | API client generated services | build | `npm run build` | ✅ | ✅ green |
| 3-01-04 | 03 | 1 | R17 | Localization XLF catalog ready | build | `npx ng extract-i18n` | ✅ | ✅ green |
| 3-01-05 | 03 | 1 | R8, R18 | Responsive catalog layout matching specs | unit | `npx ng test --watch=false` | ✅ | ✅ green |
| 3-01-06 | 03 | 1 | R4, R6 | Admin actions and CRUD forms active | unit | `npx ng test --watch=false` | ✅ | ✅ green |
| 3-01-09 | 03 | 1 | R16 | Code linting and style validation active | lint | `npx ng lint && npx stylelint "**/*.scss"` | ✅ | ✅ green |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [x] Install node dependencies and check CLI package managers.

---

## Manual-Only Verifications

- [x] Verify responsive catalog view on mobile/tablet viewports inside browser inspector.
- [x] Verify file upload UI modal responds correctly during CSV import actions.

---

## Validation Sign-Off

- [x] All tasks have `<automated>` build verify or Wave 0 dependencies
- [x] i18n XLF extract compiles correctly
- [x] No watch-mode flags
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** approved
