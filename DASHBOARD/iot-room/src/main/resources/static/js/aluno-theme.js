(function () {
    const storageKey = "iotroom.aluno.darkmode";
    const buttonId = "alunoThemeToggle";

    function applyTheme(isDark) {
        document.body.classList.toggle("aluno-dark", isDark);

        const button = document.getElementById(buttonId);

        if (!button) {
            return;
        }

        const icon = button.querySelector(".theme-toggle-icon");
        const label = button.querySelector(".theme-toggle-label");

        if (icon) {
            icon.textContent = isDark ? "☀" : "☾";
        }

        if (label) {
            label.textContent = isDark ? "Modo claro" : "Modo escuro";
        }

        button.setAttribute("aria-pressed", String(isDark));
    }

    function initTheme() {
        const savedValue = localStorage.getItem(storageKey);
        applyTheme(savedValue === "true");
    }

    document.addEventListener("click", function (event) {
        const button = event.target.closest("#" + buttonId);

        if (!button) {
            return;
        }

        event.preventDefault();

        const nextValue = !document.body.classList.contains("aluno-dark");

        localStorage.setItem(storageKey, String(nextValue));
        applyTheme(nextValue);
    });

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", initTheme);
    } else {
        initTheme();
    }
})();