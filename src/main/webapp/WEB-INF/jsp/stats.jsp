<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="header.jsp" %>

<style>
    .stats-head { display: flex; justify-content: space-between; gap: 16px; align-items: flex-start; margin-bottom: 20px; }
    .metric-card { border: 1px solid #e9ecef; border-radius: 8px; background: #fff; height: 100%; }
    .metric-value { font-size: 28px; font-weight: 700; line-height: 1.1; }
    .chart-box { min-height: 380px; }
    .fallback-pie { width: 220px; height: 220px; border-radius: 50%; margin: 20px auto; background: conic-gradient(#dee2e6 0 100%); }
    .legend-row { display: flex; justify-content: space-between; gap: 12px; padding: 6px 0; border-bottom: 1px solid #f1f3f5; }
    .legend-dot { width: 10px; height: 10px; border-radius: 50%; display: inline-block; margin-right: 8px; }
    .bar-row { margin-bottom: 14px; }
    .bar-track { height: 18px; border-radius: 999px; background: #edf2f7; overflow: hidden; }
    .bar-fill { height: 100%; background: #0d6efd; }
    @media (max-width: 768px) {
        .stats-head { display: block; }
    }
</style>

<div class="stats-head">
    <div>
        <h3><i class="bi bi-bar-chart"></i> 详细统计</h3>
        <p class="text-muted mb-0">展示题库分类及活动切片占比，以及各知识分类的活动切片数量。</p>
    </div>
    <% if (adminUser) { %>
    <a class="btn btn-outline-primary" href="/knowledge-list"><i class="bi bi-files"></i> 返回文档管理</a>
    <% } %>
</div>

<div class="row g-3 mb-4">
    <div class="col-md-3">
        <div class="metric-card p-3">
            <div class="metric-value" id="documentCount">-</div>
            <div class="text-muted">文档总数</div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="metric-card p-3">
            <div class="metric-value" id="activeDocumentCount">-</div>
            <div class="text-muted">可检索文档</div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="metric-card p-3">
            <div class="metric-value" id="chunkCount">-</div>
            <div class="text-muted">活动切片</div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="metric-card p-3">
            <div class="metric-value text-truncate" id="topCategory">-</div>
            <div class="text-muted">切片最多分类</div>
        </div>
    </div>
</div>

<div class="row g-4">
    <div class="col-lg-5">
        <div class="card h-100">
            <div class="card-header bg-white">
                <strong>题库分类及活动切片占比</strong>
            </div>
            <div class="card-body">
                <div id="categoryPieChart" class="chart-box"></div>
            </div>
        </div>
    </div>
    <div class="col-lg-7">
        <div class="card h-100">
            <div class="card-header bg-white">
                <strong>各知识分类切片数量</strong>
            </div>
            <div class="card-body">
                <div id="categoryBarChart" class="chart-box"></div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/echarts@5.5.0/dist/echarts.min.js"></script>
<script>
var chartColors = ['#0d6efd', '#198754', '#ffc107', '#dc3545', '#6f42c1', '#20c997', '#fd7e14', '#0dcaf0'];

$(function() {
    $.get('/api/knowledge/stats', function(res) {
        if (res.code === 200 && res.data) {
            var data = res.data;
            var categories = data.categoryStats || [];
            $('#documentCount').text(data.documentCount || 0);
            $('#activeDocumentCount').text(data.activeDocumentCount || 0);
            $('#chunkCount').text(data.chunkCount || 0);
            $('#topCategory').text(categories.length ? categories[0].category : '-');
            renderPie(categories);
            renderBar(categories);
        }
    });
});

function renderPie(categories) {
    if (!categories.length) {
        renderEmpty('#categoryPieChart', '暂无分类数据');
        return;
    }
    if (typeof echarts !== 'undefined') {
        echarts.init(document.getElementById('categoryPieChart')).setOption({
            tooltip: {
                trigger: 'item',
                formatter: '{b}<br/>活动切片：{c}<br/>占比：{d}%'
            },
            legend: { bottom: 0 },
            series: [{
                type: 'pie',
                radius: ['42%', '70%'],
                center: ['50%', '42%'],
                data: categories.map(function(item) {
                    return { name: item.category, value: item.chunkCount };
                })
            }]
        });
        return;
    }
    renderFallbackPie(categories);
}

function renderBar(categories) {
    if (!categories.length) {
        renderEmpty('#categoryBarChart', '暂无分类数据');
        return;
    }
    if (typeof echarts !== 'undefined') {
        echarts.init(document.getElementById('categoryBarChart')).setOption({
            tooltip: { trigger: 'axis' },
            grid: { left: 56, right: 24, top: 24, bottom: 64 },
            xAxis: {
                type: 'category',
                data: categories.map(function(item) { return item.category; }),
                axisLabel: { interval: 0, rotate: 28 }
            },
            yAxis: { type: 'value', minInterval: 1 },
            series: [{
                name: '活动切片数',
                type: 'bar',
                data: categories.map(function(item, index) {
                    return { value: item.chunkCount, itemStyle: { color: chartColors[index % chartColors.length] } };
                }),
                barMaxWidth: 42
            }]
        });
        return;
    }
    renderFallbackBars(categories);
}

function renderFallbackPie(categories) {
    var total = sum(categories, 'chunkCount');
    var start = 0;
    var segments = categories.map(function(item, index) {
        var end = start + (total ? item.chunkCount * 100 / total : 0);
        var segment = chartColors[index % chartColors.length] + ' ' + start + '% ' + end + '%';
        start = end;
        return segment;
    }).join(', ');
    var legend = categories.map(function(item, index) {
        var share = total ? Math.round(item.chunkCount * 100 / total) : 0;
        return '<div class="legend-row">'
            + '<span><i class="legend-dot" style="background:' + chartColors[index % chartColors.length] + '"></i>' + escapeHtml(item.category) + '</span>'
            + '<span>' + item.chunkCount + ' / ' + share + '%</span>'
            + '</div>';
    }).join('');
    $('#categoryPieChart').html('<div class="fallback-pie" style="background: conic-gradient(' + segments + ');"></div>' + legend);
}

function renderFallbackBars(categories) {
    var max = Math.max.apply(null, categories.map(function(item) { return item.chunkCount; }));
    var html = categories.map(function(item, index) {
        var width = max ? Math.max(6, item.chunkCount * 100 / max) : 0;
        return '<div class="bar-row">'
            + '<div class="d-flex justify-content-between mb-1"><span>' + escapeHtml(item.category) + '</span><span>' + item.chunkCount + '</span></div>'
            + '<div class="bar-track"><div class="bar-fill" style="width:' + width + '%;background:' + chartColors[index % chartColors.length] + '"></div></div>'
            + '</div>';
    }).join('');
    $('#categoryBarChart').html(html);
}

</script>

<%@ include file="footer.jsp" %>
