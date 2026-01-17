# Sample Swing UI (User & Case Management)

This is a small standalone Java Swing sample added to the project for demonstration.

Files added under `src/main/java/es/deusto/sd/user_interface/swingui`:

- `model/User.java` - simple user model
- `model/CaseItem.java` - simple case model
- `manager/UserManager.java` - in-memory user management
- `manager/CaseManager.java` - in-memory case management and simulated analysis
- `SwingApp.java` - the Swing GUI with a split pane for user and case management

Run instructions:

- Easiest: open the project in your IDE (IntelliJ IDEA, Eclipse) and run the `main` method in `es.deusto.sd.user_interface.swingui.SwingApp`.
- Command line (requires JDK installed): compile and run from project root using PowerShell:

```powershell
# Compile
Get-ChildItem -Path src/main/java -Recurse -Filter "*.java" | ForEach-Object { $_.FullName } | \
  ForEach-Object { & javac -d out $_ }

# Run
& java -cp out es.deusto.sd.user_interface.swingui.SwingApp
```

If you want, I can add a Gradle `run` task to build and run the Swing app from `./gradlew runSwing`.

Notes:
- This is a local in-memory demo: users and cases are not persisted (intentionally simple).
- Passwords are stored in plain text for the demo (do not do this in production).