// These interactions are previews only. There are no book-writing requests.
const deleteDialog = document.querySelector("#delete-dialog");

if (deleteDialog) {
    const title = deleteDialog.querySelector("#delete-book-title");
    const cancel = deleteDialog.querySelector(".dialog-cancel");
    const close = deleteDialog.querySelector(".dialog-close");
    let opener;

    document.querySelectorAll(".delete-trigger").forEach((button) => {
        button.addEventListener("click", () => {
            opener = button;
            title.textContent = button.dataset.bookTitle;
            deleteDialog.showModal();
            cancel.focus();
        });
    });

    cancel.addEventListener("click", () => deleteDialog.close());
    close.addEventListener("click", () => deleteDialog.close());
    deleteDialog.addEventListener("close", () => opener?.focus());
}

// Give optional field-level feedback while visitors explore the disabled form.
document.querySelectorAll(".book-form [required]").forEach((input) => {
    const error = document.querySelector(`#${input.id}-error`);
    const update = () => {
        const invalid = !input.checkValidity();
        input.classList.toggle("is-invalid", invalid);
        input.setAttribute("aria-invalid", String(invalid));
        error.textContent = invalid ? input.validationMessage : "";
    };
    input.addEventListener("blur", update);
    input.addEventListener("input", () => {
        if (input.classList.contains("is-invalid")) update();
    });
    input.addEventListener("change", () => {
        if (input.classList.contains("is-invalid")) update();
    });
});
