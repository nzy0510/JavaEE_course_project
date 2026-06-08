(function (window, document) {
    if (window.jQuery && window.$) {
        return;
    }

    function LiteQuery(items) {
        Array.prototype.push.apply(this, items || []);
    }

    LiteQuery.prototype = Object.create(Array.prototype);
    LiteQuery.prototype.constructor = LiteQuery;

    LiteQuery.prototype.on = function (event, handler) {
        this.forEach(function (el) { el.addEventListener(event, handler); });
        return this;
    };

    LiteQuery.prototype.attr = function (name, value) {
        if (value === undefined) {
            return this[0] ? this[0].getAttribute(name) : undefined;
        }
        this.forEach(function (el) { el.setAttribute(name, value); });
        return this;
    };

    LiteQuery.prototype.val = function (value) {
        if (value === undefined) {
            return this[0] ? this[0].value : undefined;
        }
        this.forEach(function (el) { el.value = value; });
        return this;
    };

    LiteQuery.prototype.text = function (value) {
        if (value === undefined) {
            return this[0] ? this[0].textContent : undefined;
        }
        this.forEach(function (el) { el.textContent = value; });
        return this;
    };

    LiteQuery.prototype.html = function (value) {
        if (value === undefined) {
            return this[0] ? this[0].innerHTML : undefined;
        }
        this.forEach(function (el) { el.innerHTML = value; });
        return this;
    };

    LiteQuery.prototype.append = function (value) {
        this.forEach(function (el) { el.insertAdjacentHTML('beforeend', value); });
        return this;
    };

    LiteQuery.prototype.prepend = function (value) {
        this.forEach(function (el) { el.insertAdjacentHTML('afterbegin', value); });
        return this;
    };

    LiteQuery.prototype.addClass = function (classes) {
        var names = String(classes || '').split(/\s+/).filter(Boolean);
        this.forEach(function (el) { el.classList.add.apply(el.classList, names); });
        return this;
    };

    LiteQuery.prototype.removeClass = function (classes) {
        var names = String(classes || '').split(/\s+/).filter(Boolean);
        this.forEach(function (el) { el.classList.remove.apply(el.classList, names); });
        return this;
    };

    LiteQuery.prototype.prop = function (name, value) {
        if (value === undefined) {
            return this[0] ? this[0][name] : undefined;
        }
        this.forEach(function (el) { el[name] = value; });
        return this;
    };

    function $(selector) {
        if (typeof selector === 'function') {
            if (document.readyState === 'loading') {
                document.addEventListener('DOMContentLoaded', selector);
            } else {
                selector();
            }
            return new LiteQuery([]);
        }
        if (selector instanceof LiteQuery) {
            return selector;
        }
        if (selector instanceof Element || selector === window || selector === document) {
            return new LiteQuery([selector]);
        }
        return new LiteQuery(Array.from(document.querySelectorAll(selector)));
    }

    function serializeParams(data) {
        if (!data) {
            return '';
        }
        if (typeof data === 'string') {
            return data;
        }
        return new URLSearchParams(data).toString();
    }

    $.ajax = function (options) {
        var method = (options.type || options.method || 'GET').toUpperCase();
        var url = options.url;
        var headers = {};
        var body;

        if (method === 'GET' && options.data) {
            var query = serializeParams(options.data);
            url += (url.indexOf('?') >= 0 ? '&' : '?') + query;
        } else if (options.data instanceof FormData) {
            body = options.data;
        } else if (options.data !== undefined) {
            if (options.contentType === 'application/json') {
                headers['Content-Type'] = 'application/json';
                body = options.data;
            } else if (options.contentType !== false) {
                headers['Content-Type'] = 'application/x-www-form-urlencoded;charset=UTF-8';
                body = serializeParams(options.data);
            } else {
                body = options.data;
            }
        }

        fetch(url, { method: method, headers: headers, body: body })
            .then(function (response) {
                if (!response.ok) {
                    throw response;
                }
                var contentType = response.headers.get('content-type') || '';
                if (options.dataType === 'json' || contentType.indexOf('application/json') >= 0) {
                    return response.json();
                }
                return response.text();
            })
            .then(function (data) {
                if (options.success) {
                    options.success(data);
                }
            })
            .catch(function (error) {
                if (options.error) {
                    options.error(error);
                }
            })
            .finally(function () {
                if (options.complete) {
                    options.complete();
                }
            });
    };

    $.get = function (url, data, success) {
        if (typeof data === 'function') {
            success = data;
            data = undefined;
        }
        return $.ajax({ url: url, type: 'GET', data: data, success: success, dataType: 'json' });
    };

    $.post = function (url, data, success) {
        if (typeof data === 'function') {
            success = data;
            data = undefined;
        }
        return $.ajax({ url: url, type: 'POST', data: data, success: success, dataType: 'json' });
    };

    window.$ = window.jQuery = $;
})(window, document);
