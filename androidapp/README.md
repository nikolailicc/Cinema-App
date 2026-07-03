# CineBook — Android aplikacija za rezervaciju filmova

Android (Java) klijent za **RIS Movies API** backend (Spring Boot, Basic Auth).
Projekat rađen za predmet RMA — sadrži dva layout-a (telefon/tablet) i četiri
dodatna Android koncepta pored obaveznog rada sa internet servisom.

## Pokretanje

1. Otvoriti folder `CineBook` u Android Studio (File → Open).
2. Sačekati Gradle sync (Android Studio će sam ponuditi da doda `gradlew` wrapper ako nedostaje).
3. **Podesiti adresu backend-a** u fajlu
   `app/src/main/java/com/example/cinebook/api/RetrofitClient.java`:
   - `10.0.2.2:8080` — ako testirate na **Android emulatoru**, a backend je na istom računaru (ovo je podrazumevano podešeno).
   - IP adresa računara u lokalnoj mreži (npr. `192.168.0.x:8080`) — ako testirate na **fizičkom uređaju**.
4. Mapa koristi **Leaflet.js (OpenStreetMap)** kroz `WebView` — nije potreban nikakav
   API ključ, samo internet konekcija na uređaju/emulatoru.
5. Pokrenuti backend (`localhost:8080`) pre pokretanja aplikacije.
6. Pokrenuti aplikaciju na emulatoru sa **telefon** profilom i posebno na emulatoru/AVD-u
   sa **tablet** profilom (npr. Pixel Tablet ili Nexus 10) da se vidi razlika u layout-u.

## Struktura projekta

```
model/          Movie, User, Reservation — tačno prema backend šemama
api/            ApiService (Retrofit interfejs), RetrofitClient (Basic Auth interceptor)
provider/       WatchlistProvider (Content Provider) + pomoćne klase
background/     ImageUploadTask — upload slike u pozadinskom thread-u
notification/   ReminderScheduler, ReservationReminderReceiver, BootReceiver
ui/             Activity/Fragment klase (ekrani)
ui/adapter/     RecyclerView adapteri
util/           SessionManager (čuvanje kredencijala)
```

## Dva layout-a (telefon / tablet)

- `res/layout/activity_main.xml` — **telefon**: jedan panel, lista filmova.
  Klik na film otvara `MovieDetailActivity` u novom ekranu.
- `res/layout-sw600dp/activity_main.xml` — **tablet**: master-detail dva panela
  jedan pored drugog. Klik na film prikazuje `MovieDetailFragment` odmah pored liste,
  bez otvaranja novog ekrana.
- Android sam bira odgovarajući layout na osnovu širine ekrana (`sw600dp` kvalifikator).

## Android koncepti za odbranu (pored obaveznog REST poziva)

### 1. Content Provider — lokalni watchlist
- `provider/WatchlistContract.java`, `WatchlistDbHelper.java`, `WatchlistProvider.java`
- Implementiran kao pravi `ContentProvider` nad SQLite bazom (`SQLiteOpenHelper`),
  registrovan u `AndroidManifest.xml` (authority `com.example.cinebook.provider`).
- Kada korisnik doda/ukloni film sa watchlist-e (REST poziv ka `/watchlist/{movieId}`),
  aplikacija paralelno upisuje/briše zapis i u ovom provideru
  (`WatchlistLocalStore.java`), tako da se watchlist status filmova (zvezdica na listi)
  prikazuje i bez internet konekcije.

### 2. Rad sa thread-ovima — upload slike filma
- `background/ImageUploadTask.java`
- Koristi `ExecutorService` (pozadinski thread) da:
  1. iskopira izabranu sliku iz `Uri` u privremeni fajl,
  2. sinhrono pošalje `multipart/form-data` zahtev ka `/movies/{id}/image`,
  3. rezultat vrati na UI thread preko `Handler(Looper.getMainLooper())`.
- Koristi se u `AddEditMovieActivity` (ADMIN funkcionalnost) — bira se slika iz
  galerije i šalje na server bez blokiranja UI-ja.

### 3. Notifikacije — podsetnik za rezervaciju
- `notification/ReminderScheduler.java` — koristi `AlarmManager.setExactAndAllowWhileIdle`
  da zakaže alarm u 9h ujutru na dan prikazivanja filma (`screeningDate`).
- `notification/ReservationReminderReceiver.java` — `BroadcastReceiver` koji se budi
  kad alarm okine i prikazuje `Notification` preko `NotificationManager`.
- `notification/BootReceiver.java` — hvata `BOOT_COMPLETED` (mesto za ponovno
  zakazivanje alarma nakon restarta uređaja).
- Notifikacioni kanal se kreira u `CineBookApp.java` (Application klasa).

### 4. Mapa — lokacija bioskopa
- `ui/CinemaMapActivity.java` + `res/layout/activity_map.xml` + `assets/map.html`
- Mapa je implementirana preko **Leaflet.js** (OpenStreetMap tile-ovi) unutar `WebView`-a
  — ne zahteva Google Play Services niti API ključ, samo internet konekciju.
  `WebView` učitava lokalni `assets/map.html` koji renderuje mapu i marker sa popup-om
  (naziv i adresa bioskopa). Aplikacija je koncipirana za jedan bioskop, pa je lokacija
  fiksna (nije deo backend modela).
- Dugme **"Navigiraj"** šalje `geo:` Intent koji otvara podrazumevanu navigacionu
  aplikaciju na uređaju (ili OpenStreetMap rutu u browseru ako nijedna nije instalirana)
  — interakcija korisnika sa mapom/lokacijom.

## Ostale napomene

- **Autentifikacija**: Basic Auth — korisničko ime/lozinka se čuvaju u
  `SharedPreferences` (`SessionManager`) i automatski dodaju u header svakog
  zahteva preko `AuthInterceptor`-a u `RetrofitClient`.
- **Uloge**: `User.role` (`ADMIN`/`USER`) kontroliše vidljivost admin akcija
  (dodavanje/izmena/brisanje filma, upload slike) u `MainActivity` i `MovieDetailFragment`.
- Neki endpoint-i u swagger specifikaciji imaju generičku (`Object`) strukturu tela
  zahteva/odgovora (npr. komentari, ocene, watchlist status) — u kodu su ova mesta
  označena komentarom i implementirana preko `Map<String,Object>`, uz mogućnost
  lakog prilagođavanja tačnim nazivima polja kada se backend proveri uživo.
