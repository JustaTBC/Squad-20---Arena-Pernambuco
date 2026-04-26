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

    initArenaCalendars();

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
        const cards = document.querySelectorAll('.evento-card');
        if (cards.length > 0) {
            gsap.from(cards, {
                opacity: 0,
                y: 30,
                duration: 0.6,
                stagger: 0.08,
                ease: 'power2.out',
                clearProps: 'opacity,transform'
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

    function initArenaCalendars() {
        document.querySelectorAll('[data-arena-calendar]').forEach(input => {
            if (!input.matches('input[type="date"], input[type="datetime-local"]') || input.dataset.arenaCalendarReady === 'true') {
                return;
            }

            input.dataset.arenaCalendarReady = 'true';
            const includesTime = input.type === 'datetime-local';
            const state = createCalendarState(input, includesTime);
            const field = document.createElement('div');
            field.className = 'arena-date-field';
            input.parentNode.insertBefore(field, input);
            field.appendChild(input);
            input.classList.add('arena-date-input-native');

            const trigger = document.createElement('button');
            trigger.type = 'button';
            trigger.className = 'arena-date-trigger';
            trigger.setAttribute('aria-expanded', 'false');
            trigger.setAttribute('aria-haspopup', 'dialog');
            const label = input.id ? document.querySelector(`label[for="${CSS.escape(input.id)}"]`) : null;
            if (label) {
                if (!label.id) label.id = `${input.id}-label`;
                trigger.setAttribute('aria-labelledby', label.id);
            }
            const describedBy = input.getAttribute('aria-describedby') || findFieldErrorId(input);
            if (describedBy) {
                trigger.setAttribute('aria-describedby', describedBy);
            }
            trigger.setAttribute('aria-invalid', input.classList.contains('is-invalid') ? 'true' : 'false');
            trigger.innerHTML = '<span class="arena-date-trigger__text"></span><svg class="arena-date-trigger__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4M8 2v4M3 10h18"/></svg>';
            field.appendChild(trigger);

            const popup = document.createElement('div');
            popup.className = 'arena-calendar';
            popup.hidden = true;
            popup.setAttribute('role', 'dialog');
            popup.setAttribute('aria-label', includesTime ? 'Selecionar data e hora' : 'Selecionar data');
            field.appendChild(popup);

            const render = () => renderArenaCalendar(input, state, trigger, popup, includesTime);
            render();

            trigger.addEventListener('click', () => {
                const open = popup.hidden;
                closeArenaCalendars(popup);
                popup.hidden = !open;
                trigger.setAttribute('aria-expanded', String(open));
            });

            document.addEventListener('click', event => {
                const path = event.composedPath();
                if (!path.includes(field)) {
                    popup.hidden = true;
                    trigger.setAttribute('aria-expanded', 'false');
                }
            });

            field.addEventListener('keydown', event => {
                if (event.key === 'Escape') {
                    popup.hidden = true;
                    trigger.setAttribute('aria-expanded', 'false');
                    trigger.focus();
                }
            });

            input.form?.addEventListener('reset', () => {
                window.setTimeout(() => {
                    const resetState = createCalendarState(input, includesTime);
                    state.selected = resetState.selected;
                    state.view = resetState.view;
                    state.time = resetState.time;
                    render();
                }, 0);
            });
        });
    }

    function findFieldErrorId(input) {
        const error = input.parentElement?.querySelector('.error-msg');
        if (!error) return '';
        if (!error.id) {
            error.id = `${input.id || input.name || 'arena-date'}-error`;
        }
        return error.id;
    }

    function createCalendarState(input, includesTime) {
        const parsed = parseArenaDate(input.value, includesTime);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        return {
            selected: parsed.date,
            view: parsed.date ? new Date(parsed.date.getFullYear(), parsed.date.getMonth(), 1) : new Date(today.getFullYear(), today.getMonth(), 1),
            time: parsed.time || '18:00'
        };
    }

    function renderArenaCalendar(input, state, trigger, popup, includesTime) {
        popup.replaceChildren();
        trigger.querySelector('.arena-date-trigger__text').textContent = formatArenaTrigger(state.selected, state.time, includesTime);

        const header = document.createElement('div');
        header.className = 'arena-calendar__header';
        const prev = createCalendarNav('‹', 'Mes anterior');
        const next = createCalendarNav('›', 'Proximo mes');
        const month = document.createElement('div');
        month.className = 'arena-calendar__month';
        month.textContent = state.view.toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });
        header.append(prev, month, next);
        popup.appendChild(header);

        prev.addEventListener('click', () => {
            state.view = new Date(state.view.getFullYear(), state.view.getMonth() - 1, 1);
            renderArenaCalendar(input, state, trigger, popup, includesTime);
        });
        next.addEventListener('click', () => {
            state.view = new Date(state.view.getFullYear(), state.view.getMonth() + 1, 1);
            renderArenaCalendar(input, state, trigger, popup, includesTime);
        });

        const weekdays = document.createElement('div');
        weekdays.className = 'arena-calendar__weekdays';
        ['D', 'S', 'T', 'Q', 'Q', 'S', 'S'].forEach(day => {
            const item = document.createElement('span');
            item.className = 'arena-calendar__weekday';
            item.textContent = day;
            weekdays.appendChild(item);
        });
        popup.appendChild(weekdays);

        const grid = document.createElement('div');
        grid.className = 'arena-calendar__grid';
        buildArenaDays(state.view).forEach(date => {
            const dayButton = document.createElement('button');
            dayButton.type = 'button';
            dayButton.className = 'arena-calendar__day';
            dayButton.textContent = String(date.getDate());
            if (date.getMonth() !== state.view.getMonth()) dayButton.classList.add('is-muted');
            if (isSameArenaDate(date, new Date())) dayButton.classList.add('is-today');
            if (state.selected && isSameArenaDate(date, state.selected)) dayButton.classList.add('is-selected');
            dayButton.addEventListener('click', () => {
                state.selected = new Date(date.getFullYear(), date.getMonth(), date.getDate());
                state.view = new Date(date.getFullYear(), date.getMonth(), 1);
                updateArenaInput(input, state, includesTime);
                renderArenaCalendar(input, state, trigger, popup, includesTime);
                if (!includesTime) {
                    popup.hidden = true;
                    trigger.setAttribute('aria-expanded', 'false');
                }
            });
            grid.appendChild(dayButton);
        });
        popup.appendChild(grid);

        const footer = document.createElement('div');
        footer.className = 'arena-calendar__footer';
        const today = document.createElement('button');
        today.type = 'button';
        today.className = 'arena-calendar__today';
        today.textContent = 'Hoje';
        today.addEventListener('click', () => {
            const now = new Date();
            state.selected = new Date(now.getFullYear(), now.getMonth(), now.getDate());
            state.view = new Date(now.getFullYear(), now.getMonth(), 1);
            updateArenaInput(input, state, includesTime);
            renderArenaCalendar(input, state, trigger, popup, includesTime);
        });
        footer.appendChild(today);

        if (includesTime) {
            const time = document.createElement('input');
            time.type = 'time';
            time.className = 'arena-calendar__time';
            time.value = state.time;
            time.addEventListener('change', () => {
                state.time = time.value || '18:00';
                updateArenaInput(input, state, includesTime);
                trigger.querySelector('.arena-date-trigger__text').textContent = formatArenaTrigger(state.selected, state.time, includesTime);
            });
            footer.appendChild(time);
        }

        popup.appendChild(footer);
    }

    function createCalendarNav(text, label) {
        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'arena-calendar__nav';
        button.setAttribute('aria-label', label);
        button.textContent = text;
        return button;
    }

    function buildArenaDays(view) {
        const start = new Date(view.getFullYear(), view.getMonth(), 1);
        start.setDate(start.getDate() - start.getDay());
        return Array.from({ length: 42 }, (_, index) => {
            const date = new Date(start);
            date.setDate(start.getDate() + index);
            return date;
        });
    }

    function parseArenaDate(value, includesTime) {
        if (!value) return { date: null, time: '' };
        const [datePart, timePart = ''] = value.split('T');
        const [year, month, day] = datePart.split('-').map(Number);
        if (!year || !month || !day) return { date: null, time: timePart };
        return {
            date: new Date(year, month - 1, day),
            time: includesTime ? timePart.slice(0, 5) : ''
        };
    }

    function updateArenaInput(input, state, includesTime) {
        if (!state.selected) return;
        const dateValue = toArenaIsoDate(state.selected);
        input.value = includesTime ? `${dateValue}T${state.time || '18:00'}` : dateValue;
        input.dispatchEvent(new Event('input', { bubbles: true }));
        input.dispatchEvent(new Event('change', { bubbles: true }));
    }

    function formatArenaTrigger(date, time, includesTime) {
        if (!date) return includesTime ? 'dd/mm/aaaa --:--' : 'dd/mm/aaaa';
        const value = date.toLocaleDateString('pt-BR');
        return includesTime ? `${value} ${time || '18:00'}` : value;
    }

    function toArenaIsoDate(date) {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }

    function isSameArenaDate(a, b) {
        return a.getFullYear() === b.getFullYear() &&
            a.getMonth() === b.getMonth() &&
            a.getDate() === b.getDate();
    }

    function closeArenaCalendars(except) {
        document.querySelectorAll('.arena-calendar').forEach(calendar => {
            if (calendar !== except) {
                calendar.hidden = true;
                calendar.parentElement?.querySelector('.arena-date-trigger')?.setAttribute('aria-expanded', 'false');
            }
        });
    }
});
