# ParkPulse -- Offline-First Park Survey Android App

> **Take-home assignment status:** In progress\
> **Implementation progress:** Approximately **75--80%** based on the
> current development status. This is a self-assessment, not an official
> evaluation score.

ParkPulse is an Android application for surveying parks. It is designed
to let a field user view park boundaries, create park polygons, record
survey information, and work with limited or no internet connectivity.

## 1. Project Overview

The app is being developed in **Kotlin** with **Jetpack Compose**. It
follows an MVVM-oriented structure and uses a local Room database so
core park and survey workflows can be developed around local data.

### Main goals

-   Authenticate a field user with a mock login flow.
-   Display parks and their survey status on a map and in a searchable
    list.
-   Create park boundaries by selecting points on the map.
-   Calculate polygon area and perimeter.
-   Record park survey details.
-   Capture survey photos from inside the app.
-   Support offline data capture and synchronization when connectivity
    returns.

## 2. Technology Stack

  -----------------------------------------------------------------------
  Area                                Technology
  ----------------------------------- -----------------------------------
  Language                            Kotlin

  UI                                  Jetpack Compose

  Architecture                        MVVM

  Dependency injection                Hilt

  Navigation                          Navigation Compose

  Local database                      Room

  Map                                 MapLibre

  Camera                              CameraX

  Session persistence                 DataStore

  Location                            Fused Location Provider
                                      (planned/used for location
                                      workflows)
  -----------------------------------------------------------------------

> Update this table if your final Gradle dependencies or implementation
> differ. Do not list a library as implemented merely because it is
> planned.

## 3. Implemented Features

The following features have been implemented in the current project
version:

-   **Mock login and session persistence:** login flow and local session
    persistence using DataStore.
-   **Parks list:** park listing with search and filter functionality.
-   **Local database:** Room integration for local app data.
-   **Map display:** MapLibre map with park polygons displayed using
    survey-status colours. The map and polygon display have been tested
    on a device.
-   **Create Park:** select at least three points on the map to draw a
    park boundary, calculate its area and perimeter, and save the park
    locally. This flow has been tested on a device.
-   **Survey form:** survey form UI and input flow.
-   **Camera flow:** CameraX preview, photo capture, and review screen.

## 4. Current Progress and Remaining Work

This section is intentionally transparent about what is complete and
what still needs validation.

  -----------------------------------------------------------------------
  Requirement                         Current state
  ----------------------------------- -----------------------------------
  Mock login and persistent session   Implemented

  Parks list, search and filters      Implemented

  Room local database                 Integrated

  MapLibre map and status-coloured    Implemented and device-tested
  polygons                            

  Create park polygon                 Implemented and device-tested

  Area and perimeter calculation      Implemented

  Survey form                         Implemented

  CameraX capture and review          Implemented

  Attach confirmed photo back to      **Issue under investigation:**
  survey form                         after selecting "Use Photo", the
                                      app returns to the form but the
                                      captured photo is not yet displayed
                                      there

  Geofence validation                 Requires testing: user must be
                                      inside the park polygon and GPS
                                      accuracy must be 25 metres or
                                      better

  Offline survey and photo            Requires end-to-end validation
  persistence                         

  Mock-server synchronization and     Requires end-to-end validation
  retry behavior                      

  Unit tests                          To be added/run for geometry and
                                      location calculations

  Final README, clean build and       Submission preparation
  release APK                         
  -----------------------------------------------------------------------

**Progress estimate: approximately 75--80%.** The estimate reflects
implemented UI and core workflows. It does not mean every requirement
has passed end-to-end testing.

## 5. Setup and Run

1.  Clone the repository:

    ``` bash
    git clone https://github.com/<YOUR_GITHUB_USERNAME>/<YOUR_REPOSITORY_NAME>.git
    ```

2.  Open the project in Android Studio.

3.  Allow Gradle to sync and download the required dependencies.

4.  Confirm that the required Android SDK and build tools are installed.

5.  Connect an Android device or start an emulator.

6.  Run the `app` configuration.

If the project requires any local configuration, document it here
without committing API keys, passwords, signing files, or other secrets.

## 6. Demo Login

The app currently uses a mock login flow.

-   **Username/email:** `<ADD_DEMO_USERNAME_IF_CONFIGURED>`
-   **Password:** `<ADD_DEMO_PASSWORD_IF_CONFIGURED>`

Replace these placeholders with the actual demo credentials before
submission, if the app has fixed credentials. Do not publish personal or
production credentials.

## 7. Map and Geometry Calculations

Park boundaries are represented as polygon coordinates and displayed on
the map.

The Create Park flow lets the user select boundary points. The app
calculates:

-   **Area:** the estimated surface area enclosed by the polygon.
-   **Perimeter:** the total length around the polygon boundary.

The calculations should use geographic coordinates and earth-surface
distance/area methods rather than treating latitude and longitude as
ordinary flat Cartesian coordinates.

Add the exact algorithm or library used in the final implementation
here, and document the coordinate order. For GeoJSON, coordinate pairs
are conventionally stored as `[longitude, latitude]`; Android location
objects commonly expose latitude and longitude as separate values.

## 8. Offline Data and Synchronization

The app integrates Room for local persistence. The intended
offline-first workflow is:

1.  Read and write app data locally.
2.  Allow field data capture without relying on a continuous network
    connection.
3.  Queue unsynchronized records.
4.  Synchronize pending records with the mock server when connectivity
    is available or when the user triggers **Sync now**.
5.  Retain failed records for a later retry.

**Current status:** Room is integrated, but the complete offline
capture, photo persistence, retry, and server synchronization flow still
requires end-to-end testing. Update this section after verifying the
full workflow.

## 9. Permissions and Location

The app needs location access for the user's current position and survey
check-in validation, and camera access for survey photographs.

The intended survey check-in rule is:

-   The user's location must be inside the selected park polygon.
-   GPS-reported horizontal accuracy must be **25 metres or better**.

Permissions should be requested when the related feature is first used,
with a clear explanation. Denied permissions and permanent denial should
be handled gracefully, including a route to app Settings when
appropriate.

**Before submission:** verify the permission flow on a real device,
including denial and "Don't ask again" cases.

## 10. Tests

The assignment calls for tests around the core geographic and check-in
logic. Add and run tests for:

-   Polygon area.
-   Polygon perimeter.
-   Point-in-polygon.
-   Distance and compass direction.
-   Check-in eligibility (inside polygon and accuracy ≤ 25 m).

**Current status:** these tests are still part of the remaining
submission work. Do not mark them as passing until they have been run
successfully.

## 11. Build an APK

To create a debug APK from Android Studio:

1.  Open the project.

2.  Select **Build → Build Bundle(s) / APK(s) → Build APK(s)**.

3.  Wait for the build to finish.

4.  Select **Locate** in the build notification.

5.  The debug APK is normally generated at:

    ``` text
    app/build/outputs/apk/debug/app-debug.apk
    ```

You can also build it from the project root in a terminal:

``` bash
# Windows
gradlew.bat assembleDebug

# macOS / Linux
./gradlew assembleDebug
```

For a release APK, configure a release signing key securely and build:

``` bash
# Windows
gradlew.bat assembleRelease

# macOS / Linux
./gradlew assembleRelease
```

The release APK is normally generated at:

``` text
app/build/outputs/apk/release/app-release.apk
```

Do not commit your keystore, passwords, or signing credentials. Keep
them outside the repository and configure them securely. Verify the APK
installs and launches on a device before publishing it.

## 12. Publish the APK on GitHub Releases

1.  Push the source code to your public GitHub repository.
2.  Open the repository on GitHub.
3.  Select **Releases** (usually from the right-hand side or the
    repository's **Releases** page).
4.  Choose **Create a new release**.
5.  Create a version tag, for example `v1.0.0`.
6.  Add a title such as `ParkPulse Android APK`.
7.  Add a short release description, including known limitations.
8.  Attach the APK file (`app-debug.apk` for a debug build or the signed
    `app-release.apk` for a release build).
9.  Publish the release.

Prefer a signed release APK for final delivery if the assignment expects
a distributable build. Clearly label a debug APK if that is what you
upload.

## 13. Push the README and Project to GitHub

If the repository is already initialized locally, save this file as
`README.md` in the **root folder** of the project. Then run:

``` bash
git status
git add README.md
git commit -m "docs: add project README"
git push origin main
```

If your default branch is named `master`, use:

``` bash
git push origin master
```

To push the project files as well as the README:

``` bash
git add .
git commit -m "Add ParkPulse Android application"
git push -u origin main
```

If you have not connected the local project to GitHub yet:

``` bash
git init
git branch -M main
git remote add origin https://github.com/<YOUR_GITHUB_USERNAME>/<YOUR_REPOSITORY_NAME>.git
git add .
git commit -m "Initial ParkPulse project commit"
git push -u origin main
```

If the remote repository already contains a README or other initial
commit, pull and reconcile the remote history before pushing rather than
force-pushing.

## 14. Security Checklist Before Pushing

-   Do not commit API keys, passwords, access tokens, or personal
    credentials.
-   Do not commit signing keystores or signing passwords.
-   Check `local.properties`, `keystore.properties`, and any
    secrets/config files.
-   Review `git status` and staged files before committing.
-   If a secret was committed previously, deleting it in a later commit
    is not enough; remove it from Git history and rotate the exposed
    secret.

## 15. Suggested Submission Checklist

-   [ ] Public GitHub repository is accessible.
-   [ ] README setup and demo-login instructions are accurate.
-   [ ] Project builds successfully from a clean checkout.
-   [ ] App installs and launches on a device.
-   [ ] Photo is correctly attached to the survey after tapping **Use
    Photo**.
-   [ ] Geofence and GPS accuracy rules are verified.
-   [ ] Offline save and synchronization are tested end-to-end.
-   [ ] Required unit tests are added and passing.
-   [ ] No secrets or signing credentials are present in Git history.
-   [ ] APK is attached to a GitHub Release.
-   [ ] Repository contains clear, meaningful commits.

## 16. AI Assistance Disclosure

AI tools were used to assist with development and/or documentation. The
implementation, integration, testing, and final verification should be
reviewed by the author. Update this statement to accurately describe
which AI tools were used and what they helped with, in accordance with
the assignment instructions.
