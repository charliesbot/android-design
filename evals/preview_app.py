"""Finds the previewer app's executable, which the runner renders with and the agent under test reaches over MCP."""
import os
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
EXECUTABLE = Path("AndroidDesignPreview.app/Contents/MacOS/AndroidDesignPreview")
REPO_BUILD = ROOT / "app/app/build/compose/binaries/main/app" / EXECUTABLE
SERVER_NAME = "android_design_preview"


def app_binary(environ=None, home=None):
    """The previewer's executable: $ANDROID_DESIGN_PREVIEW, then ~/Applications, then the repo's build output."""
    environ = os.environ if environ is None else environ
    home = Path.home() if home is None else Path(home)
    override = environ.get("ANDROID_DESIGN_PREVIEW")
    if override:
        if not Path(override).is_file():
            raise RuntimeError(f"ANDROID_DESIGN_PREVIEW points to {override}, which is not a file")
        return Path(override)
    candidates = [home / "Applications" / EXECUTABLE, REPO_BUILD]
    for candidate in candidates:
        if candidate.is_file():
            return candidate
    searched = "\n".join(f"  {c}" for c in candidates)
    raise RuntimeError(
        "The previewer app is not installed. Run `./gradlew :app:installApp` from app/, or set "
        f"ANDROID_DESIGN_PREVIEW to its executable. Looked in:\n{searched}")
