(function () {
    const scrollKey = "iotroom.scroll:" + window.location.pathname;
    const minLoaderTime = 220;

    let loaderStartedAt = 0;

    function ensureLoader() {
        let loader = document.getElementById("keepScrollLoader");

        if (!loader) {
            loader = document.createElement("div");
            loader.id = "keepScrollLoader";
            loader.className = "keep-scroll-loader";
            loader.innerHTML = `
                <div class="keep-scroll-loader-card">
                    <div class="keep-scroll-spinner"></div>
                    <span>A carregar...</span>
                </div>
            `;
            document.body.appendChild(loader);
        }

        return loader;
    }

    function showLoader() {
        const loader = ensureLoader();
        loaderStartedAt = Date.now();

        requestAnimationFrame(function () {
            loader.classList.add("is-active");
        });
    }

    function hideLoader() {
        const loader = document.getElementById("keepScrollLoader");

        if (!loader) {
            return;
        }

        const elapsed = Date.now() - loaderStartedAt;
        const delay = Math.max(0, minLoaderTime - elapsed);

        setTimeout(function () {
            loader.classList.remove("is-active");
        }, delay);
    }

    function saveScroll() {
        sessionStorage.setItem(scrollKey, String(window.scrollY));
        showLoader();
    }

    function isIgnoredLink(link) {
        const href = (link.getAttribute("href") || "").toLowerCase();

        return href.includes("/exportar")
            || href.includes(".xlsx")
            || href.includes(".csv")
            || href.includes("/logout")
            || href.startsWith("mailto:")
            || href.startsWith("tel:")
            || href.startsWith("#");
    }

    if ("scrollRestoration" in history) {
        history.scrollRestoration = "manual";
    }

    window.addEventListener("load", function () {
        const savedScroll = sessionStorage.getItem(scrollKey);

        if (savedScroll !== null) {
            const targetY = parseInt(savedScroll, 10);

            sessionStorage.removeItem(scrollKey);

            if (Number.isNaN(targetY)) {
                return;
            }

            showLoader();

            setTimeout(function () {
                window.scrollTo(0, targetY);
                hideLoader();
            }, 90);
        }
    });

    document.addEventListener("DOMContentLoaded", function () {
        document.querySelectorAll("form.keep-scroll-form").forEach(function (form) {
            form.addEventListener("submit", function () {
                saveScroll();
            });
        });

        document.querySelectorAll("a.keep-scroll").forEach(function (link) {
            link.addEventListener("click", function () {
                if (isIgnoredLink(link)) {
                    return;
                }

                saveScroll();
            });
        });
    });
})();