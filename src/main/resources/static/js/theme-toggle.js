/**
 * Dark/Light Theme Toggle
 */

(function() {
    'use strict';

    const themeToggle = document.getElementById('themeToggle');
    const currentTheme = localStorage.getItem('theme') || 'light';

    function setTheme(theme) {
        if (theme === 'dark') {
            document.documentElement.setAttribute('data-theme', 'dark');
            if (themeToggle) themeToggle.checked = true;
        } else {
            document.documentElement.setAttribute('data-theme', 'light');
            if (themeToggle) themeToggle.checked = false;
        }
        localStorage.setItem('theme', theme);
    }

    setTheme(currentTheme);

    if (themeToggle) {
        themeToggle.addEventListener('change', function() {
            const newTheme = this.checked ? 'dark' : 'light';
            setTheme(newTheme);
        });
    }

})();
