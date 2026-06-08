(function (window, $) {
    function escapeHtml(text) {
        return String(text || '')
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function statusBadge(status) {
        if (status === 'ACTIVE') return '<span class="badge bg-success">可检索</span>';
        if (status === 'ARCHIVED') return '<span class="badge bg-secondary">已归档</span>';
        if (status === 'FAILED') return '<span class="badge bg-danger">失败</span>';
        return '<span class="badge bg-info">处理中</span>';
    }

    function formatSize(value) {
        if (!value) return '0 B';
        if (value < 1024) return value + ' B';
        if (value < 1024 * 1024) return (value / 1024).toFixed(1) + ' KB';
        return (value / 1024 / 1024).toFixed(1) + ' MB';
    }

    function formatDate(value) {
        return value ? value.substring(0, 19).replace('T', ' ') : '-';
    }

    function truncateText(text, maxLength) {
        text = String(text || '');
        return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
    }

    function sum(items, field) {
        return (items || []).reduce(function (total, item) {
            return total + (item[field] || 0);
        }, 0);
    }

    function renderEmpty(selector, text) {
        $(selector).html('<div class="text-center text-muted pt-5">' + escapeHtml(text || '暂无数据') + '</div>');
    }

    window.escapeHtml = escapeHtml;
    window.statusBadge = statusBadge;
    window.formatSize = formatSize;
    window.formatDate = formatDate;
    window.truncateText = truncateText;
    window.sum = sum;
    window.renderEmpty = renderEmpty;
})(window, window.jQuery);
