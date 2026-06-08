<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="header.jsp" %>

<style>
    .dashboard-head { display: flex; justify-content: space-between; gap: 16px; align-items: flex-start; margin-bottom: 22px; }
    .metric-card { border: 1px solid #e9ecef; border-radius: 8px; background: #fff; height: 100%; }
    .metric-card .metric-icon { width: 38px; height: 38px; border-radius: 8px; display: inline-flex; align-items: center; justify-content: center; background: #eef5ff; color: #0d6efd; margin-bottom: 12px; }
    .metric-card .metric-value { font-size: 30px; font-weight: 700; line-height: 1.1; }
    .quick-action { border: 1px solid #e9ecef; border-radius: 8px; padding: 16px; text-decoration: none; color: #212529; display: block; height: 100%; background: #fff; }
    .quick-action:hover { border-color: #0d6efd; color: #0d6efd; }
    .quick-action i { font-size: 22px; margin-right: 8px; }
    .table td, .table th { vertical-align: middle; }
    @media (max-width: 768px) {
        .dashboard-head { display: block; }
    }
</style>

<div class="dashboard-head">
    <div>
        <h3><i class="bi bi-house"></i> 文档知识库首页</h3>
        <p class="text-muted mb-0">查看知识库整体状态，进入文档导入、文档管理、AI 问答和详细统计。</p>
    </div>
    <a class="btn btn-primary" href="/knowledge-add"><i class="bi bi-cloud-upload"></i> 上传文档</a>
</div>

<div class="row g-3 mb-4">
    <div class="col-md-3">
        <div class="metric-card p-3">
            <div class="metric-icon"><i class="bi bi-file-earmark-text"></i></div>
            <div class="metric-value" id="documentCount">-</div>
            <div class="text-muted">文档总数</div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="metric-card p-3">
            <div class="metric-icon"><i class="bi bi-check2-circle"></i></div>
            <div class="metric-value" id="activeDocumentCount">-</div>
            <div class="text-muted">可检索文档</div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="metric-card p-3">
            <div class="metric-icon"><i class="bi bi-layers"></i></div>
            <div class="metric-value" id="chunkCount">-</div>
            <div class="text-muted">活动切片</div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="metric-card p-3">
            <div class="metric-icon"><i class="bi bi-tags"></i></div>
            <div class="metric-value" id="categoryCount">-</div>
            <div class="text-muted">知识类型</div>
        </div>
    </div>
</div>

<div class="row g-4 mb-4">
    <div class="col-md-3">
        <a class="quick-action" href="/knowledge-add"><i class="bi bi-cloud-upload"></i><strong>上传文档</strong><div class="text-muted small mt-2">导入 PDF、Word、Markdown 等资料</div></a>
    </div>
    <div class="col-md-3">
        <a class="quick-action" href="/knowledge-list"><i class="bi bi-files"></i><strong>文档管理</strong><div class="text-muted small mt-2">查询、查看切片、归档和删除文档</div></a>
    </div>
    <div class="col-md-3">
        <a class="quick-action" href="/ai-qa"><i class="bi bi-robot"></i><strong>AI 问答</strong><div class="text-muted small mt-2">基于活动切片召回生成答案</div></a>
    </div>
    <div class="col-md-3">
        <a class="quick-action" href="/stats"><i class="bi bi-bar-chart"></i><strong>详细统计</strong><div class="text-muted small mt-2">查看题库分类与切片分布图表</div></a>
    </div>
</div>

<div class="card">
    <div class="card-header bg-white d-flex justify-content-between align-items-center">
        <strong>最近导入文档</strong>
        <a class="small text-decoration-none" href="/knowledge-list">查看全部</a>
    </div>
    <div class="card-body">
        <div class="table-responsive">
            <table class="table table-sm align-middle">
                <thead>
                <tr>
                    <th>文件名</th>
                    <th>知识分类</th>
                    <th>类型</th>
                    <th>切片数</th>
                    <th>状态</th>
                    <th>导入时间</th>
                </tr>
                </thead>
                <tbody id="recentDocs"></tbody>
            </table>
        </div>
    </div>
</div>

<script>
$(function() {
    $.get('/api/knowledge/stats', function(res) {
        if (res.code === 200 && res.data) {
            $('#documentCount').text(res.data.documentCount || 0);
            $('#activeDocumentCount').text(res.data.activeDocumentCount || 0);
            $('#chunkCount').text(res.data.chunkCount || 0);
            $('#categoryCount').text((res.data.categoryStats || []).length);
        }
    });
    $.get('/api/knowledge/documents?size=6', function(res) {
        if (res.code === 200 && res.data) {
            var rows = '';
            res.data.records.forEach(function(item) {
                rows += '<tr>'
                    + '<td class="fw-semibold">' + escapeHtml(item.originalFilename) + '</td>'
                    + '<td><span class="badge bg-light text-dark border">' + escapeHtml(item.knowledgeCategory || '通用知识') + '</span></td>'
                    + '<td>' + escapeHtml(item.fileExtension) + '</td>'
                    + '<td>' + item.chunkCount + '</td>'
                    + '<td>' + statusBadge(item.status) + '</td>'
                    + '<td>' + formatDate(item.createTime) + '</td>'
                    + '</tr>';
            });
            $('#recentDocs').html(rows || '<tr><td colspan="6" class="text-center text-muted">暂无文档</td></tr>');
        }
    });
});

</script>

<%@ include file="footer.jsp" %>
