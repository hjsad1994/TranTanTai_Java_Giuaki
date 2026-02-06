/**
 * ═══════════════════════════════════════════════════════════════════════════
 * BOOKHAVEN ADMIN - Core JavaScript
 * Handles sidebar, navigation, and common admin functionality
 * ═══════════════════════════════════════════════════════════════════════════
 */

(function() {
    'use strict';

    // ═══════════════════════════════════════════════════════════════════════════
    // SIDEBAR ACTIVE STATE - Auto-detect based on current URL
    // ═══════════════════════════════════════════════════════════════════════════
    function initActiveMenu() {
        const currentPath = window.location.pathname;
        const navLinks = document.querySelectorAll('.nav-link[data-menu]');

        // Remove all active states first
        navLinks.forEach(link => link.classList.remove('active'));

        // Determine active menu based on URL
        let activeMenu = 'dashboard';

        if (currentPath === '/admin' || currentPath === '/admin/') {
            activeMenu = 'dashboard';
        } else if (currentPath.startsWith('/admin/books')) {
            activeMenu = 'books';
        } else if (currentPath.startsWith('/admin/categories')) {
            activeMenu = 'categories';
        } else if (currentPath.startsWith('/admin/inventory')) {
            activeMenu = 'inventory';
        } else if (currentPath.startsWith('/admin/orders')) {
            activeMenu = 'orders';
        } else if (currentPath.startsWith('/admin/users')) {
            activeMenu = 'users';
        } else if (currentPath.startsWith('/admin/statistics')) {
            activeMenu = 'statistics';
        } else if (currentPath.startsWith('/admin/reports')) {
            activeMenu = 'reports';
        } else if (currentPath.startsWith('/admin/settings')) {
            activeMenu = 'settings';
        }

        // Set active class
        const activeLink = document.querySelector(`.nav-link[data-menu="${activeMenu}"]`);
        if (activeLink) {
            activeLink.classList.add('active');
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SIDEBAR TOGGLE - With localStorage persistence
    // ═══════════════════════════════════════════════════════════════════════════
    function initSidebarToggle() {
        const sidebar = document.querySelector('.admin-sidebar');
        const main = document.querySelector('.admin-main');
        const toggleBtn = document.getElementById('sidebarToggle');
        const STORAGE_KEY = 'admin-sidebar-collapsed';

        // Restore collapsed state from localStorage
        const isCollapsed = localStorage.getItem(STORAGE_KEY) === 'true';
        if (isCollapsed) {
            sidebar?.classList.add('collapsed');
            main?.classList.add('expanded');
        }

        // Toggle sidebar
        toggleBtn?.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();

            sidebar?.classList.toggle('collapsed');
            main?.classList.toggle('expanded');

            // Save state to localStorage
            const nowCollapsed = sidebar?.classList.contains('collapsed');
            localStorage.setItem(STORAGE_KEY, nowCollapsed);
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MOBILE TOGGLE
    // ═══════════════════════════════════════════════════════════════════════════
    function initMobileToggle() {
        const mobileToggle = document.getElementById('mobileToggle');
        const sidebar = document.querySelector('.admin-sidebar');
        const overlay = document.getElementById('sidebarOverlay');

        mobileToggle?.addEventListener('click', function() {
            sidebar?.classList.toggle('mobile-open');
            overlay?.classList.toggle('active');
        });

        // Overlay click closes sidebar
        overlay?.addEventListener('click', function() {
            sidebar?.classList.remove('mobile-open');
            this.classList.remove('active');
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SEARCH SHORTCUT (Ctrl/Cmd + K)
    // ═══════════════════════════════════════════════════════════════════════════
    function initSearchShortcut() {
        document.addEventListener('keydown', function(e) {
            if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
                e.preventDefault();
                document.querySelector('.topbar-search-input')?.focus();
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // INITIALIZE
    // ═══════════════════════════════════════════════════════════════════════════
    function init() {
        initActiveMenu();
        initSidebarToggle();
        initMobileToggle();
        initSearchShortcut();
    }

    // Run on DOM ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
