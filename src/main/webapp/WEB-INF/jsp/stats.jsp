<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="header.jsp" %>

<h3><i class="bi bi-bar-chart"></i> 数据统计</h3>
<p class="text-muted">知识库分类分布与状态概览</p>

<div class="row g-4 mb-4">
    <div class="col-md-6">
        <div class="card">
            <div class="card-header">分类分布（饼图）</div>
            <div class="card-body">
                <div id="pieChart" style="height: 400px;"></div>
            </div>
        </div>
    </div>
    <div class="col-md-6">
        <div class="card">
            <div class="card-header">分类分布（柱状图）</div>
            <div class="card-body">
                <div id="barChart" style="height: 400px;"></div>
            </div>
        </div>
    </div>
</div>

<div class="row g-4">
    <div class="col-md-6">
        <div class="card">
            <div class="card-header">难度分布</div>
            <div class="card-body">
                <div id="difficultyChart" style="height: 350px;"></div>
            </div>
        </div>
    </div>
    <div class="col-md-6">
        <div class="card">
            <div class="card-header">状态占比</div>
            <div class="card-body">
                <div id="statusChart" style="height: 350px;"></div>
            </div>
        </div>
    </div>
</div>

<script>
$(function() {
    $.get('/api/knowledge/list?size=999', function(res) {
        if (res.code === 200 && res.data) {
            var records = res.data.records;

            var catMap = {};
            var diffMap = {};
            var statusMap = {};
            records.forEach(function(item) {
                catMap[item.category] = (catMap[item.category] || 0) + 1;
                diffMap[item.difficulty || '未标注'] = (diffMap[item.difficulty || '未标注'] || 0) + 1;
                statusMap[item.status] = (statusMap[item.status] || 0) + 1;
            });

            var catData = Object.entries(catMap).map(function(e) { return {name: e[0], value: e[1]}; });
            var diffData = Object.entries(diffMap).map(function(e) { return {name: e[0], value: e[1]}; });
            var statusData = Object.entries(statusMap).map(function(e) { return {name: e[0] === 'ACTIVE' ? '活跃' : '已归档', value: e[1]}; });

            var pieOpt = { tooltip: { trigger: 'item' }, series: [{ type: 'pie', radius: '65%', data: catData }] };
            var barOpt = {
                tooltip: { trigger: 'axis' },
                xAxis: { type: 'category', data: catData.map(function(d) { return d.name; }), axisLabel: { rotate: 30 } },
                yAxis: { type: 'value' },
                series: [{ type: 'bar', data: catData.map(function(d) { return d.value; }), itemStyle: { color: '#0d6efd' } }]
            };
            var diffOpt = { tooltip: { trigger: 'item' }, series: [{ type: 'pie', radius: '65%', data: diffData, label: { formatter: '{b}: {c}' } }] };
            var statusOpt = {
                tooltip: { trigger: 'item' },
                series: [{ type: 'pie', radius: '65%', data: statusData, label: { formatter: '{b}: {c} ({d}%)' },
                    itemStyle: { color: function(p) { return p.name === '活跃' ? '#198754' : '#6c757d'; } }
                }]
            };

            echarts.init(document.getElementById('pieChart')).setOption(pieOpt);
            echarts.init(document.getElementById('barChart')).setOption(barOpt);
            echarts.init(document.getElementById('difficultyChart')).setOption(diffOpt);
            echarts.init(document.getElementById('statusChart')).setOption(statusOpt);
        }
    });
});
</script>

<%@ include file="footer.jsp" %>
