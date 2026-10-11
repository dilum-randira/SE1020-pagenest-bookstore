const menuButton = document.querySelector(".menu-toggle");
const navigation = document.querySelector(".primary-navigation");

function closeMenu() {
    navigation.classList.remove("is-open");
    menuButton.setAttribute("aria-expanded", "false");
    menuButton.setAttribute("aria-label", "Open menu");
}

menuButton.addEventListener("click", () => {
    const isOpen = navigation.classList.toggle("is-open");
    menuButton.setAttribute("aria-expanded", String(isOpen));
    menuButton.setAttribute("aria-label", isOpen ? "Close menu" : "Open menu");
});

navigation.querySelectorAll("a").forEach((link) => {
    link.addEventListener("click", closeMenu);
});

document.addEventListener("keydown", (event) => {
    if (event.key === "Escape" && navigation.classList.contains("is-open")) {
        closeMenu();
        menuButton.focus();
    }
});

document.addEventListener("click", (event) => {
    if (!navigation.contains(event.target) && !menuButton.contains(event.target)) {
        closeMenu();
    }
});

const categoryButtons = document.querySelectorAll(".category-button");
const bookCards = document.querySelectorAll(".book-card");
const resultsNote = document.querySelector("#results-note");

categoryButtons.forEach((button) => {
    button.addEventListener("click", () => {
        const selectedCategory = button.dataset.category;
        let visibleCount = 0;

        categoryButtons.forEach((categoryButton) => {
            const isSelected = categoryButton === button;
            categoryButton.classList.toggle("is-active", isSelected);
            categoryButton.setAttribute("aria-pressed", String(isSelected));
        });

        bookCards.forEach((card) => {
            const isVisible = selectedCategory === "all" || card.dataset.bookCategory === selectedCategory;
            card.hidden = !isVisible;
            if (isVisible) visibleCount++;
        });

        const categoryName = button.textContent.replace("↗", "").trim();
        resultsNote.textContent = selectedCategory === "all"
            ? `Showing all ${visibleCount} preview books`
            : `Showing ${visibleCount} ${categoryName.toLowerCase()} preview ${visibleCount === 1 ? "book" : "books"}`;

        document.querySelector("#featured").scrollIntoView({
            behavior: window.matchMedia("(prefers-reduced-motion: reduce)").matches ? "auto" : "smooth"
        });
    });
});

// Keep the CSS cover visible if its Open Library image does not load.
document.querySelectorAll("[data-cover-image]").forEach((image) => {
    const showFallback = () => { image.hidden = true; };
    image.addEventListener("error", showFallback);
    if (image.complete && image.naturalWidth === 0) showFallback();
});
