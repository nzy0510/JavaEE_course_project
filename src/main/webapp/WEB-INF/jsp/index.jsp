<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="header.jsp" %>

<div class="row mb-4">
    <div class="col">
        <h3><i class="bi bi-house"></i> 知识库仪表盘</h3>
        <p class="text-muted">欢迎使用知识库管理系统，管理和查询您的技术知识条目，使用 AI 辅助问答。</p>
    </div>
</div>

<div class="row g-4 mb-4">
    <div class="col-md-3">
        <div class="card text-white bg-primary">
            <div class="card-body">
                <h5 class="card-title"><i class="bi bi-book"></i></h5>
                <p class="card-text fs-3" id="totalCount">-</p>
                <p class="card-text">知识条目总数</p>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card text-white bg-success">
            <div class="card-body">
                <h5 class="card-title"><i class="bi bi-check-circle"></i></h5>
                <p class="card-text fs-3" id="activeCount">-</p>
                <p class="card-text">活跃条目</p>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card text-white bg-warning">
            <div class="card-body">
                <h5 class="card-title"><i class="bi bi-archive"></i></h5>
                <p class="card-text fs-3" id="archivedCount">-</p>
                <p class="card-text">已归档条目</p>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card text-white bg-info">
            <div class="card-body">
                <h5 class="card-title"><i class="bi bi-diagram-3"></i></h5>
                <p class="card-text fs-3" id="categoryCount">-</p>
                <p class="card-text">分类数量</p>
            </div>
        </div>
    </div>
</div>

<div class="row g-4">
    <div class="col-md-8">
        <div class="card">
            <div class="card-header">分类分布</div>
            <div class="card-body">
                <div id="categoryChart" style="height: 400px;"></div>
            </div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="card">
            <div class="card-header">快捷入口</div>
            <div class="card-body">
                <div class="d-grid gap-2">
                    <a href="/knowledge-add" class="btn btn-outline-primary">
                        <i class="bi bi-plus-circle"></i> 添加知识条目
                    </a>
                    <a href="/knowledge-add" class="btn btn-outline-success">
                        <i class="bi bi-file-earmark-arrow-up"></i> 批量导入 JSON
                    </a>
                    <a href="/knowledge-list" class="btn btn-outline-info">
                        <i class="bi bi-search"></i> 查询管理知识
                    </a>
                    <a href="/ai-qa" class="btn btn-outline-warning">
                        <i class="bi bi-robot"></i> AI 智能问答
                    </a>
                    <a href="/stats" class="btn btn-outline-secondary">
                        <i class="bi bi-bar-chart"></i> 详细统计
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
$(function() {
    $.get('/api/knowledge/list?size=1', function(res) {
        if (res.code === 200 && res.data) {
            $('#totalCount').text(res.data.total);
        }
    });
    $.get('/api/knowledge/list?status=ACTIVE&size=1', function(res) {
        if (res.code === 200 && res.data) {
            $('#activeCount').text(res.data.total);
        }
    });
    $.get('/api/knowledge/list?status=ARCHIVED&size=1', function(res) {
        if (res.code === 200 && res.data) {
            $('#archivedCount').text(res.data.total);
        }
    });

    $.get('/api/knowledge/list?size=999', function(res) {
        if (res.code === 200 && res.data) {
            var cats = {};
            res.data.records.forEach(function(item) {
                cats[item.category] = (cats[item.category] || 0) + 1;
            });
            $('#categoryCount').text(Object.keys(cats).length);

            var chart = echarts.init(document.getElementById('categoryChart'));
            chart.setOption({
                tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
                series: [{
                    type: 'pie',
                    radius: ['40%', '70%'],
                    data: Object.entries(cats).map(function(e) { return {name: e[0], value: e[1]}; })
                }]
            });
        }
    });
});
</script>

<%@ include file="footer.jsp" %>
