// Opens the dialog on load: the default views must not pass because a modal hides the page.
customElements.whenDefined('m3e-dialog').then(() => document.getElementById('credits').show());
