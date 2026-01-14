# 🛡️ ZGuard

![Version](https://img.shields.io/badge/version-1.0.0--Beta--3-orange)
![Platform](https://img.shields.io/badge/platform-Paper%20(1.8--1.21)%20%7C%20Velocity-blue)
![Requires](https://img.shields.io/badge/requires-ZCore-red)

**ZGuard** to zaawansowany system ochrony przed VPN i Proxy, stworzony z myślą o maksymalnej kompatybilności. Dzięki zastosowaniu nowoczesnych bibliotek, plugin wspiera serwery od wersji **1.8** aż do najnowszych wydań **1.21+**, a także silnik proxy **Velocity**.

## ✨ Kluczowe cechy
- 🔄 **Pełna Kompatybilność**: Wspiera wersje 1.8 - 1.21+.
- 🚄 **Velocity Ready**: Działa natywnie na proxy, chroniąc całą sieć serwerów w jednym punkcie.
- 💎 **Modern UI**: Obsługa kolorów Hex i gradientów (MiniMessage) na każdej wersji serwera dzięki ZCore.
- ⚡ **Asynchroniczność**: Sprawdzanie adresów IP nie powoduje "lagów" serwera (TPS drops).

## 🛠️ Komendy i Uprawnienia

Główna komenda: `/zguard` (alias: `/zg`)

| Komenda | Opis | Uprawnienie |
| :--- | :--- | :--- |
| `/zg help` | Wyświetla listę dostępnych komend | `zguard.admin` |
| `/zg info` | Status ochrony, platforma i wersja | `zguard.admin` |
| `/zg reload` | Przeładowuje konfigurację i pliki języków | `zguard.admin` |

### 🛡️ Uprawnienia specjalne

| Uprawnienie | Opis | Domyślnie |
| :--- | :--- | :--- |
| `zguard.*` | Pełny dostęp do wszystkich permisji pluginu | `OP` |
| `zguard.admin` | Pozwala na zarządzanie pluginem (reload, info) | `OP` |
| `zguard.bypass` | **Omija sprawdzanie VPN/Proxy.** Pozwala graczowi wejść na serwer mimo aktywnej ochrony. | `OP` |

> [!TIP]
> Nadaj uprawnienie `zguard.bypass` zaufanym graczom lub administracji, którzy muszą korzystać z VPN, aby uniknąć ich przypadkowego zablokowania przez system ochrony.

## 📦 Instalacja
1. Pobierz i zainstaluj **[ZCore](https://github.com/THEzombiePL/ZCore)**.
2. Pobierz **ZGuard** i wrzuć do folderu `plugins`.
3. Skonfiguruj wiadomości w `messages_pl.yml` lub `messages_en.yml`.

---
Created with ❤️ by **THEzombiePL**