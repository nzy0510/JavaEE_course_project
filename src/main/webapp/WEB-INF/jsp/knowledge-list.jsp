<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="header.jsp" %>

<h3><i class="bi bi-list-ul"></i> 知识条目管理</h3>
<p class="text-muted">查询、编辑、归档和恢复知识条目</p>

<!-- 搜索栏 -->
<div class="card mb-4">
    <div class="card-body">
        <form id="searchForm" class="row g-2 align-items-end">
            <div class="col-md-3">
                <label class="form-label">关键词</label>
                <input type="text" class="form-control" name="keyword" placeholder="搜索标题/内容/标签">
            </div>
            <div class="col-md-2">
                <label class="form-label">分类</label>
                <select class="form-select" name="category" id="searchCategory">
                    <option value="">全部</option>
                </select>
            </div>
            <div class="col-md-2">
                <label class="form-label">状态</label>
                <select class="form-select" name="status">
                    <option value="">全部</option>
                    <option value="ACTIVE">活跃</option>
                    <option value="ARCHIVED">已归档</option>
                </select>
            </div>
            <div class="col-md-2">
                <button type="submit" class="btn btn-primary">
                    <i class="bi bi-search"></i> 搜索
                </button>
            </div>
        </form>
    </div>
</div>

<!-- 列表 -->
<div class="card">
    <div class="card-body">
        <div id="totalInfo" class="mb-2 text-muted"></div>
        <div class="table-responsive">
            <table class="table table-hover">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>标题</th>
                        <th>分类</th>
                        <th>难度</th>
                        <th>状态</th>
                        <th>更新时间</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody id="tableBody"></tbody>
            </table>
        </div>
        <nav id="pagination"></nav>
    </div>
</div>

<!-- 编辑弹窗 -->
<div class="modal fade" id="editModal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">编辑知识条目</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <form id="editForm">
                    <input type="hidden" name="id">
                    <div class="mb-3">
                        <label class="form-label">标题 <span class="text-danger">*</span></label>
                        <input type="text" class="form-control" name="subject" required>
                    </div>
                    <div class="row mb-3">
                        <div class="col-md-6">
                            <label class="form-label">分类 <span class="text-danger">*</span></label>
                            <select class="form-select" name="category" required id="editCategory"></select>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">难度</label>
                            <select class="form-select" name="difficulty">
                                <option value="简单">简单</option>
                                <option value="中等">中等</option>
                                <option value="困难">困难</option>
                            </select>
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">标签</label>
                        <input type="text" class="form-control" name="tags">
                    </div>
                    <div class="mb-3">
                        <label class="form-label">核心内容 <span class="text-danger">*</span></label>
                        <textarea class="form-control" name="principles" rows="8" required></textarea>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">常见误区</label>
                        <textarea class="form-control" name="pitfalls" rows="3"></textarea>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                <button type="button" class="btn btn-primary" onclick="saveEdit()">保存</button>
            </div>
        </div>
    </div>
</div>

<script>
var categories = ${categoriesJson};
var currentPage = 1;

$(function() {
    var html = '';
    categories.forEach(function(c) {
        html += '<option value="' + c + '">' + c + '</option>';
    });
    $('#searchCategory').append(html);
    $('#editCategory').html('<option value="">请选择</option>' + html);
    loadList();
});

$('#searchForm').on('submit', function(e) {
    e.preventDefault();
    currentPage = 1;
    loadList();
});

function loadList() {
    var params = {
        page: currentPage,
        size: 10,
        keyword: $('[name=keyword]').val(),
        category: $('[name=category]').val(),
        status: $('[name=status]').val()
    };
    $.get('/api/knowledge/list', params, function(res) {
        if (res.code === 200) {
            var data = res.data;
            $('#totalInfo').text('共 ' + data.total + ' 条记录，第 ' + data.current + '/' + data.pages + ' 页');
            var rows = '';
            data.records.forEach(function(item) {
                var statusBadge = item.status === 'ACTIVE'
                    ? '<span class="badge bg-success">活跃</span>'
                    : '<span class="badge bg-secondary">已归档</span>';
                var actions = '';
                if (item.status === 'ACTIVE') {
                    actions += '<button class="btn btn-sm btn-outline-primary me-1" onclick="editAtom(' + item.id + ')">编辑</button>';
                    actions += '<button class="btn btn-sm btn-outline-warning" onclick="archiveAtom(' + item.id + ')">归档</button>';
                } else {
                    actions += '<button class="btn btn-sm btn-outline-success" onclick="restoreAtom(' + item.id + ')">恢复</button>';
                }
                rows += '<tr>'
                    + '<td>' + item.id + '</td>'
                    + '<td>' + escapeHtml(item.subject) + '</td>'
                    + '<td>' + (item.category || '-') + '</td>'
                    + '<td>' + (item.difficulty || '-') + '</td>'
                    + '<td>' + statusBadge + '</td>'
                    + '<td>' + (item.updateTime ? item.updateTime.substring(0, 10) : '-') + '</td>'
                    + '<td>' + actions + '</td>'
                    + '</tr>';
            });
            $('#tableBody').html(rows || '<tr><td colspan="7" class="text-center text-muted">暂无数据</td></tr>');
            renderPagination(data);
        }
    });
}

function renderPagination(data) {
    var html = '<ul class="pagination">';
    for (var i = 1; i <= data.pages; i++) {
        html += '<li class="page-item ' + (i === data.current ? 'active' : '') + '">'
            + '<a class="page-link" href="#" onclick="goPage(' + i + ')">' + i + '</a></li>';
    }
    html += '</ul>';
    $('#pagination').html(html);
}

function goPage(p) { currentPage = p; loadList(); }

function archiveAtom(id) {
    if (!confirm('确定归档该条目？')) return;
    $.post('/api/knowledge/archive/' + id, function(res) {
        if (res.code === 200) loadList();
    });
}

function restoreAtom(id) {
    $.post('/api/knowledge/restore/' + id, function(res) {
        if (res.code === 200) loadList();
    });
}

function editAtom(id) {
    $.get('/api/knowledge/detail/' + id, function(res) {
        if (res.code === 200) {
            var item = res.data;
            $('#editForm [name=id]').val(item.id);
            $('#editForm [name=subject]').val(item.subject);
            $('#editForm [name=category]').val(item.category);
            $('#editForm [name=difficulty]').val(item.difficulty || '中等');
            $('#editForm [name=tags]').val(item.tags || '');
            $('#editForm [name=principles]').val(item.principles);
            $('#editForm [name=pitfalls]').val(item.pitfalls || '');
            new bootstrap.Modal('#editModal').show();
        }
    });
}

function saveEdit() {
    var data = {
        id: parseInt($('#editForm [name=id]').val()),
        subject: $('#editForm [name=subject]').val(),
        category: $('#editForm [name=category]').val(),
        difficulty: $('#editForm [name=difficulty]').val(),
        tags: $('#editForm [name=tags]').val(),
        principles: $('#editForm [name=principles]').val(),
        pitfalls: $('#editForm [name=pitfalls]').val()
    };
    $.ajax({
        url: '/api/knowledge/update',
        type: 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(data),
        success: function(res) {
            if (res.code === 200) {
                bootstrap.Modal.getInstance('#editModal').hide();
                loadList();
            } else {
                alert(res.message);
            }
        }
    });
}

function escapeHtml(text) {
    if (!text) return '';
    return text.replace(/</g, '&lt;').replace(/>/g, '&gt;');
}
</script>

<%@ include file="footer.jsp" %>
