# Laboratorium 4: Kalkulator – część 1 (Interfejs, orientacja i kolorystyka)

Pierwsza część projektu kalkulatora naukowego w Jetpack Compose: responsywny układ w orientacji pionowej i poziomej oraz spójna paleta barw Material 3 obsługująca tryb jasny i ciemny.

---

## 1. Architektura siatki przycisków i pole działania (Zadanie 2)

* **Wybór sposobu budowy siatki (`Column`/`Row` z `Modifier.weight` zamiast `LazyVerticalGrid`):**  
  Zdecydowano się na zagnieżdżony układ `Column` i `Row` z modyfikatorem `Modifier.weight(1f)` oraz odstępami `Arrangement.spacedBy(8.dp)`. Liczba przycisków kalkulatora jest stała i niewielka (20 w bloku podstawowym, 8 w naukowym), dlatego leniwe ładowanie (`LazyVerticalGrid`) jest zbędne, a dodatkowo utrudnia równomierne rozciągnięcie wierszy w pionie na pełną wysokość ekranu bez przewijania. Zastosowanie wag (`weight`) gwarantuje idealne skalowanie przycisków zarówno na małym telefonie, jak i na dużym ekranie tabletu, bez użycia sztywnych wymiarów w `dp`.
* **Obsługa długich wyrażeń w polu działania:**  
  Pole wyświetlające wprowadzane działanie wyposażono w przewijanie poziome (`Modifier.horizontalScroll`) połączone z efektem `LaunchedEffect(expression)`, który automatycznie przesuwa widok do prawej krawędzi wraz z dopisywaniem kolejnych znaków. Dzięki temu nawet kilkudziesięcioznakowe wyrażenie nie znika za krawędzią ekranu.

---

## 2. Układ poziomy i tryb naukowy (Zadanie 3)

* **Brak duplikacji kodu siatki:**  
  Wszystkie przyciski opisano za pomocą modelu danych (`CalcButton` zawierający etykietę, rolę wizualną `ButtonRole` oraz akcję `CalcAction`). Stworzono jeden uniwersalny komponent `ButtonGrid`, który w orientacji pionowej renderuje listę `basicButtons`, natomiast w orientacji poziomej (rozpoznawanej przez `LocalConfiguration.current.orientation`) jest wywoływany dwukrotnie wewnątrz kontenera `Row` – dla bloku naukowego (`scientificButtons`) oraz podstawowego (`basicButtons`).
* **Dlaczego przycisk $\pi$ powinien wpisywać nazwę stałej (`pi`), a nie liczbę `3,14159`? (Odpowiedź do sprawozdania):**  
  Wpisanie zaokrąglonego tekstu `3.14159` obcina precyzję już na etapie wprowadzania danych (błąd zaokrąglenia rzędu $10^{-6}$), który w dalszych obliczeniach (np. potęgowaniu czy funkcjach trygonometrycznych) ulega wzmocnieniu, przez co np. $\sin(\pi)$ nie da dokładnie $0$. Ponadto długi ciąg cyfr zaciemnia zapis w polu działania i utrudnia jego edycję (np. kasowanie pojedynczym kliknięciem). Wpisanie symbolicznej nazwy `pi` pozwala mechanizmowi liczącemu w drugiej części projektu podstawić w momencie obliczeń pełną precyzję zmiennoprzecinkową typu `Double` (`Math.PI`, czyli 15–17 cyfr znaczących).

---

## 3. Kolorystyka i dostępność (Zadanie 4)

* **Uzasadnienie wyboru koloru bazowego (Odpowiedź do sprawozdania):**  
  Jako kolor bazowy wybrano chłodny odcień szmaragdowo-morski (`#006B5F` – *Emerald Teal*), ponieważ kojarzy się z precyzją, nowoczesnymi urządzeniami pomiarowymi i spokojem, nie męcząc wzroku przy dłuższej pracy, a jednocześnie wyraźnie odróżnia aplikację od domyślnego, fioletowego szablonu Android Studio.
* **Hierarchia ról kolorystycznych i tryb ciemny:**  
  Na podstawie koloru bazowego wygenerowano pełny zestaw ról dla trybu jasnego i ciemnego (wyłączając `dynamicColor`, aby zachować spójność projektu):
  * **Cyfry (`DIGIT`):** `surfaceVariant` / `onSurfaceVariant` – neutralne tło dla najczęściej klikanych klawiszy.
  * **Operatory (`OPERATOR`):** `secondaryContainer` / `onSecondaryContainer` – wyraźne oddzielenie kolumny działań i nawiasów.
  * **Akcje czyszczące (`ACTION` - C, ⌫):** `tertiaryContainer` / `onTertiaryContainer` – wyróżnienie przycisków modyfikujących wpis.
  * **Przycisk wyniku (`EQUALS` - `=`):** `primary` / `onPrimary` – najsilniejszy akcent wizualny na ekranie (kontrast powyżej 4.5:1 zgodnie z WCAG 2.2).

---

## 4. Zrzuty ekranu

### Orientacja pionowa (Tryb jasny i ciemny):
| Tryb jasny (Pion) | Tryb ciemny (Pion) |
| :---: | :---: |
| <img width="468" height="980" alt="Zrzut ekranu 2026-11-03 093928" src="https://github.com/user-attachments/assets/85ec77e0-424f-4999-9530-b7d414d1f602" /> | <img width="467" height="977" alt="Zrzut ekranu 2026-11-03 094052" src="https://github.com/user-attachments/assets/445b9d5e-c652-4d74-a23b-3d6ed32ded03" /> |

### Orientacja pozioma – tryb naukowy (Tryb jasny i ciemny):
| Tryb jasny (Poziom) | Tryb ciemny (Poziom) |
| :---: | :---: |
| <img width="1048" height="467" alt="Zrzut ekranu 2026-11-03 094133" src="https://github.com/user-attachments/assets/595f59b2-0491-4ee9-8536-fb0ab8196df0" />| <img width="1048" height="470" alt="Zrzut ekranu 2026-11-03 094107" src="https://github.com/user-attachments/assets/66c3a161-1466-463e-9145-bc09bd08b336" />|
