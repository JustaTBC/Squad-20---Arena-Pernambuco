document.addEventListener('DOMContentLoaded', () => {

    // === AOS Init ===
    if (typeof AOS !== 'undefined') {
        AOS.init({
            duration: 800,
            easing: 'ease-out-cubic',
            once: true,
            offset: 50
        });
    }

    // === Lenis Smooth Scroll ===
    if (typeof Lenis !== 'undefined') {
        const lenis = new Lenis({
            duration: 1.2,
            easing: (t) => Math.min(1, 1.001 - Math.pow(2, -10 * t))
        });
        function raf(time) {
            lenis.raf(time);
            requestAnimationFrame(raf);
        }
        requestAnimationFrame(raf);
    }

    // === GSAP Page Fade-In ===
    if (typeof gsap !== 'undefined') {
        gsap.from('main', {
            opacity: 0,
            y: 20,
            duration: 0.6,
            ease: 'power2.out'
        });
    }

    // === Header Scroll Effect ===
    const header = document.querySelector('.site-header');
    if (header) {
        const onScroll = () => {
            header.classList.toggle('scrolled', window.scrollY > 50);
        };
        window.addEventListener('scroll', onScroll, { passive: true });
        onScroll();
    }

    // === Mobile Menu Toggle ===
    const menuToggle = document.querySelector('.menu-toggle');
    const nav = document.querySelector('.nav');
    if (menuToggle && nav) {
        menuToggle.addEventListener('click', () => {
            nav.classList.toggle('nav--open');
            menuToggle.classList.toggle('active');
        });

        nav.querySelectorAll('a').forEach(link => {
            link.addEventListener('click', () => {
                nav.classList.remove('nav--open');
                menuToggle.classList.remove('active');
            });
        });
    }

    // === Close mobile menu on outside click ===
    document.addEventListener('click', (e) => {
        if (nav && menuToggle &&
            nav.classList.contains('nav--open') &&
            !nav.contains(e.target) &&
            !menuToggle.contains(e.target)) {
            nav.classList.remove('nav--open');
            menuToggle.classList.remove('active');
        }
    });

    // === Close mobile menu on Escape key ===
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && nav && nav.classList.contains('nav--open')) {
            nav.classList.remove('nav--open');
            menuToggle.classList.remove('active');
            menuToggle.focus();
        }
    });

    // === Active nav link based on current path ===
    const currentPath = window.location.pathname;
    document.querySelectorAll('.nav a:not(.btn)').forEach(link => {
        const href = link.getAttribute('href');
        if (href === currentPath || (href !== '/' && currentPath.startsWith(href))) {
            link.classList.add('active');
        }
    });

    // === GSAP Hover Effects for Role Cards ===
    if (typeof gsap !== 'undefined') {
        document.querySelectorAll('.role-card').forEach(card => {
            card.addEventListener('mouseenter', () => {
                gsap.to(card, { scale: 1.05, duration: 0.3, ease: 'power2.out' });
            });
            card.addEventListener('mouseleave', () => {
                gsap.to(card, { scale: 1, duration: 0.3, ease: 'power2.out' });
            });
        });
    }

    // === GSAP Stagger animation for cards ===
    if (typeof gsap !== 'undefined') {
        const cards = document.querySelectorAll('.evento-card, .card, section.card');
        if (cards.length > 0) {
            gsap.from(cards, {
                opacity: 0,
                y: 30,
                duration: 0.5,
                stagger: 0.1,
                ease: 'power2.out',
                delay: 0.2
            });
        }
    }

    // === GSAP Hero text reveal ===
    if (typeof gsap !== 'undefined') {
        const heroTitle = document.querySelector('.hero h1');
        const heroText = document.querySelector('.hero p');
        const heroCta = document.querySelector('.hero .cta');

        if (heroTitle) {
            const tl = gsap.timeline({ defaults: { ease: 'power3.out' } });
            tl.from(heroTitle, { opacity: 0, y: 40, duration: 0.8 })
              .from(heroText, { opacity: 0, y: 30, duration: 0.6 }, '-=0.4')
              .from(heroCta, { opacity: 0, y: 20, duration: 0.5 }, '-=0.3');
        }
    }

    // === Stats counter animation ===
    if (typeof gsap !== 'undefined') {
        const statNumbers = document.querySelectorAll('.stat__number');
        if (statNumbers.length > 0) {
            const observer = new IntersectionObserver((entries) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        const el = entry.target;
                        const target = parseInt(el.textContent, 10);
                        if (!isNaN(target)) {
                            gsap.from(el, {
                                textContent: 0,
                                duration: 1.5,
                                ease: 'power2.out',
                                snap: { textContent: 1 },
                                onUpdate: function() {
                                    el.textContent = Math.ceil(parseFloat(el.textContent));
                                }
                            });
                        }
                        observer.unobserve(el);
                    }
                });
            }, { threshold: 0.5 });

            statNumbers.forEach(el => observer.observe(el));
        }
    }
});
