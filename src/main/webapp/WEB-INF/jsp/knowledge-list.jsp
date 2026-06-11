<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="header.jsp" %>

<style>
    .page-heading { display: flex; justify-content: space-between; gap: 16px; align-items: flex-start; margin-bottom: 20px; }
    .page-heading h3 { margin-bottom: 6px; }
    .toolbar-card .form-label { font-size: 13px; color: #6c757d; margin-bottom: 6px; }
    .table td, .table th { vertical-align: middle; }
    .action-cell { min-width: 168px; white-space: nowrap; }
    .chunk-result { border: 1px solid #e9ecef; border-radius: 8px; padding: 14px 16px; margin-bottom: 12px; background: #fff; }
    .chunk-result-title { font-weight: 600; color: #212529; }
    .chunk-result-content { white-space: pre-wrap; color: #495057; margin-top: 8px; line-height: 1.65; }
    .empty-panel { border: 1px dashed #dee2e6; border-radius: 8px; padding: 32px; text-align: center; color: #6c757d; background: #fafafa; }
    @media (max-width: 768px) {
        .page-heading { display: block; }
        .action-cell { min-width: 220px; }
    }
</style>

<div class="page-heading">
    <div>
        <h3><i class="bi bi-files"></i> 文档管理</h3>
        <p class="text-muted mb-0">按文件名管理导入文档，也可按关键词检索活动切片内容。</p>
    </div>
    <% if (adminUser) { %>
    <a class="btn btn-primary" href="/knowledge-add"><i class="bi bi-cloud-upload"></i> 上传文档</a>
    <% } %>
</div>

<div class="row g-4 mb-4">
    <div class="col-lg-5">
        <div class="card toolbar-card h-100">
            <div class="card-header bg-white">
                <strong><i class="bi bi-search"></i> 文档查询</strong>
            </div>
            <div class="card-body">
                <form id="searchForm" class="row g-3 align-items-end">
                    <div class="col-md-7">
                        <label class="form-label">文件名关键词</label>
                        <input type="text" class="form-control" name="keyword" placeholder="例如 AI大模型、SpringBoot">
                    </div>
                    <div class="col-md-5">
                        <label class="form-label">状态</label>
                        <select class="form-select" name="status">
                            <option value="">全部</option>
                            <option value="ACTIVE">可检索</option>
                            <option value="ARCHIVED">已归档</option>
                            <option value="FAILED">导入失败</option>
                        </select>
                    </div>
                    <div class="col-12">
                        <button type="submit" class="btn btn-primary"><i class="bi bi-search"></i> 查询文档</button>
                        <button type="button" class="btn btn-outline-secondary ms-2" onclick="resetDocumentSearch()">重置</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
    <div class="col-lg-7">
        <div class="card toolbar-card h-100">
            <div class="card-header bg-white">
                <strong><i class="bi bi-text-paragraph"></i> 切片关键词查询</strong>
            </div>
            <div class="card-body">
                <form id="chunkSearchForm" class="row g-3 align-items-end">
                    <div class="col-md-9">
                        <label class="form-label">切片关键词</label>
                        <input type="text" class="form-control" name="chunkKeyword" placeholder="例如 RAG 流程、LoRA 微调、Transformer">
                    </div>
                    <div class="col-md-3">
                        <button type="submit" class="btn btn-outline-primary w-100"><i class="bi bi-search"></i> 查询切片</button>
                    </div>
                </form>
                <div id="chunkSearchSummary" class="text-muted small mt-3">输入关键词后，将从可检索切片中按匹配分返回结果。</div>
                <div id="chunkSearchResults" class="mt-3"></div>
            </div>
        </div>
    </div>
</div>

<div class="card">
    <div class="card-header bg-white d-flex justify-content-between align-items-center">
        <strong>文档列表</strong>
        <span id="totalInfo" class="text-muted small"></span>
    </div>
    <div class="card-body">
        <div class="table-responsive">
            <table class="table table-hover align-middle">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>文件名</th>
                    <th>知识分类</th>
                    <th>类型</th>
                    <th>大小</th>
                    <th>切片数</th>
                    <th>状态</th>
                    <th>导入时间</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody id="tableBody"></tbody>
            </table>
        </div>
        <nav id="pagination" class="mt-3"></nav>
    </div>
</div>

<div class="modal fade" id="chunkModal" tabindex="-1">
    <div class="modal-dialog modal-xl modal-dialog-scrollable">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">文档切片</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <div id="chunkList"></div>
            </div>
        </div>
    </div>
</div>

<script>
var currentPage = 1;
var isAdmin = <%= adminUser %>;

$(function() {
    loadList();
});

$('#searchForm').on('submit', function(e) {
    e.preventDefault();
    currentPage = 1;
    loadList();
});

$('#chunkSearchForm').on('submit', function(e) {
    e.preventDefault();
    searchChunks();
});

function loadList() {
    var params = {
        page: currentPage,
        size: 10,
        keyword: $('[name=keyword]').val(),
        status: $('[name=status]').val()
    };
    $.get('/api/knowledge/documents', params, function(res) {
        if (res.code === 200) {
            var data = res.data;
            $('#totalInfo').text('共 ' + data.total + ' 个文档，第 ' + data.current + '/' + data.pages + ' 页');
            var rows = '';
            data.records.forEach(function(item) {
                var actions = '<button class="btn btn-sm btn-outline-primary me-1" onclick="viewChunks(' + item.id + ')"><i class="bi bi-card-text"></i> 切片</button>';
                if (isAdmin) {
                    if (item.status === 'ACTIVE') {
                        actions += '<button class="btn btn-sm btn-outline-warning me-1" onclick="archiveDoc(' + item.id + ')">归档</button>';
                    } else if (item.status === 'ARCHIVED') {
                        actions += '<button class="btn btn-sm btn-outline-success me-1" onclick="restoreDoc(' + item.id + ')">恢复</button>';
                    }
                    actions += '<button class="btn btn-sm btn-outline-danger" onclick="deleteDoc(' + item.id + ')">删除</button>';
                }

                rows += '<tr>'
                    + '<td>' + item.id + '</td>'
                    + '<td class="fw-semibold">' + escapeHtml(item.originalFilename) + '</td>'
                    + '<td><span class="badge bg-light text-dark border">' + escapeHtml(item.knowledgeCategory || '通用知识') + '</span></td>'
                    + '<td>' + escapeHtml(item.fileExtension) + '</td>'
                    + '<td>' + formatSize(item.fileSize) + '</td>'
                    + '<td>' + item.chunkCount + '</td>'
                    + '<td>' + statusBadge(item.status) + '</td>'
                    + '<td>' + formatDate(item.createTime) + '</td>'
                    + '<td class="action-cell">' + actions + '</td>'
                    + '</tr>';
            });
            $('#tableBody').html(rows || '<tr><td colspan="9"><div class="empty-panel">暂无文档</div></td></tr>');
            renderPagination(data);
        }
    });
}

function searchChunks() {
    var keyword = $('[name=chunkKeyword]').val().trim();
    if (!keyword) {
        $('#chunkSearchSummary').text('请输入切片关键词。');
        $('#chunkSearchResults').empty();
        return;
    }
    $('#chunkSearchSummary').text('正在检索：' + keyword);
    $('#chunkSearchResults').html('<div class="empty-panel">检索中...</div>');
    $.ajax({
        url: '/api/knowledge/chunks/search',
        method: 'GET',
        dataType: 'json',
        data: { keyword: keyword, limit: 10 },
        success: function(res) {
            if (res.code !== 200) {
                $('#chunkSearchResults').html('<div class="empty-panel">检索失败</div>');
                return;
            }
            renderChunkSearchResults(res.data || []);
        }
    });
}

function renderChunkSearchResults(results) {
    $('#chunkSearchSummary').text('匹配到 ' + results.length + ' 个活动切片');
    if (!results.length) {
        $('#chunkSearchResults').html('<div class="empty-panel">暂无匹配切片</div>');
        return;
    }
    var html = results.map(function(result) {
        var chunk = result.chunk || {};
        var document = result.document || {};
        return '<div class="chunk-result">'
            + '<div class="d-flex justify-content-between gap-3">'
            + '<div class="chunk-result-title">' + escapeHtml(chunk.titlePath || '未命名切片') + '</div>'
            + '<span class="badge bg-primary-subtle text-primary border">匹配分 ' + result.score + '</span>'
            + '</div>'
            + '<div class="text-muted small mt-1">' + escapeHtml(document.originalFilename || '-') + ' · ' + escapeHtml(document.knowledgeCategory || '通用知识') + '</div>'
            + '<div class="chunk-result-content">' + escapeHtml(truncateText(chunk.content || '', 260)) + '</div>'
            + '</div>';
    }).join('');
    $('#chunkSearchResults').html(html);
}

function renderPagination(data) {
    if (!data.pages || data.pages <= 1) {
        $('#pagination').empty();
        return;
    }
    var html = '<ul class="pagination mb-0">';
    for (var i = 1; i <= data.pages; i++) {
        html += '<li class="page-item ' + (i === data.current ? 'active' : '') + '">'
            + '<a class="page-link" href="#" onclick="goPage(' + i + '); return false;">' + i + '</a></li>';
    }
    html += '</ul>';
    $('#pagination').html(html);
}

function resetDocumentSearch() {
    $('[name=keyword]').val('');
    $('[name=status]').val('');
    currentPage = 1;
    loadList();
}

function goPage(p) { currentPage = p; loadList(); }

function viewChunks(id) {
    $.get('/api/knowledge/documents/' + id + '/chunks', function(res) {
        if (res.code === 200) {
            var html = '';
            res.data.forEach(function(chunk) {
                html += '<div class="border rounded p-3 mb-3">'
                    + '<div class="text-muted small">#' + chunk.chunkIndex + ' · ' + escapeHtml(chunk.titlePath || '') + ' · 估算 token: ' + chunk.tokenEstimate + '</div>'
                    + '<pre class="mb-0 mt-2" style="white-space: pre-wrap;">' + escapeHtml(chunk.content) + '</pre>'
                    + '</div>';
            });
            $('#chunkList').html(html || '<div class="empty-panel">暂无切片</div>');
            new bootstrap.Modal('#chunkModal').show();
        }
    });
}

function archiveDoc(id) {
    if (!confirm('确定归档该文档？归档后不会参与 AI 检索。')) return;
    $.post('/api/knowledge/documents/' + id + '/archive', function(res) {
        if (res.code === 200) loadList();
    });
}

function restoreDoc(id) {
    $.post('/api/knowledge/documents/' + id + '/restore', function(res) {
        if (res.code === 200) loadList();
    });
}

function deleteDoc(id) {
    if (!confirm('确定永久删除该文档及其全部切片？')) return;
    $.ajax({
        url: '/api/knowledge/documents/' + id,
        type: 'DELETE',
        success: function(res) {
            if (res.code === 200) loadList();
        }
    });
}

</script>

<%@ include file="footer.jsp" %>
