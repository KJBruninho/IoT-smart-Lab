(function () {
    const storageKey = "iotroom.global.darkmode";
    const alunoStorageKey = "iotroom.aluno.darkmode";
    const toggleSelector = ".js-app-theme-toggle, .js-aluno-theme-toggle, #alunoThemeToggle, #alunoThemeToggleTopbar";

    function getButtons() {
        return Array.from(document.querySelectorAll(toggleSelector));
    }

    function bodyHasClass(className) {
        return document.body.classList.contains(className);
    }

    function updateButtons(isDark) {
        getButtons().forEach(function (button) {
            const label = button.querySelector(".theme-toggle-label");

            if (label) {
                label.textContent = isDark ? "Modo claro" : "Modo escuro";
            }

            button.setAttribute("aria-pressed", String(isDark));
        });
    }

    function applyTheme(isDark) {
        /*
         * Aluno usa:
         * body.aluno-dark
         *
         * Professor/Admin usam:
         * body.theme-dark
         * body.theme-light
         */
        document.body.classList.toggle("aluno-dark", isDark);
        document.body.classList.toggle("theme-dark", isDark);
        document.body.classList.toggle("theme-light", !isDark);

        localStorage.setItem(storageKey, String(isDark));
        localStorage.setItem(alunoStorageKey, String(isDark));

        updateButtons(isDark);
    }

    function getInitialTheme() {
        const savedGlobalTheme = localStorage.getItem(storageKey);

        if (savedGlobalTheme !== null) {
            return savedGlobalTheme === "true";
        }

        const savedAlunoTheme = localStorage.getItem(alunoStorageKey);

        if (savedAlunoTheme !== null) {
            return savedAlunoTheme === "true";
        }

        return bodyHasClass("aluno-dark") || bodyHasClass("theme-dark");
    }

    window.toggleAppTheme = function () {
        const isDarkNow = bodyHasClass("aluno-dark") || bodyHasClass("theme-dark");
        applyTheme(!isDarkNow);
    };

    function initTheme() {
        applyTheme(getInitialTheme());
    }

    document.addEventListener("click", function (event) {
        const button = event.target.closest(toggleSelector);

        if (!button) {
            return;
        }

        event.preventDefault();
        window.toggleAppTheme();
    });

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", initTheme);
    } else {
        initTheme();
    }
})();