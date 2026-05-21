<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="header.jsp" %>

<h3><i class="bi bi-plus-circle"></i> 添加知识条目</h3>
<p class="text-muted">支持单条表单录入或批量上传 JSON 文件</p>

<ul class="nav nav-tabs mb-4" id="addTab" role="tablist">
    <li class="nav-item">
        <button class="nav-link active" data-bs-toggle="tab" data-bs-target="#single" type="button">单条添加</button>
    </li>
    <li class="nav-item">
        <button class="nav-link" data-bs-toggle="tab" data-bs-target="#batch" type="button">批量导入 JSON</button>
    </li>
</ul>

<div class="tab-content">
    <!-- 单条添加 -->
    <div class="tab-pane fade show active" id="single">
        <div class="card">
            <div class="card-body">
                <form id="singleForm">
                    <div class="mb-3">
                        <label class="form-label">标题 <span class="text-danger">*</span></label>
                        <input type="text" class="form-control" name="subject" required placeholder="知识条目标题">
                    </div>
                    <div class="row mb-3">
                        <div class="col-md-6">
                            <label class="form-label">分类 <span class="text-danger">*</span></label>
                            <select class="form-select" name="category" required id="categorySelect"></select>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">难度</label>
                            <select class="form-select" name="difficulty">
                                <option value="简单">简单</option>
                                <option value="中等" selected>中等</option>
                                <option value="困难">困难</option>
                            </select>
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">标签 (JSON数组格式，如 ["Java","Spring"])</label>
                        <input type="text" class="form-control" name="tags" placeholder='["Java", "Spring"]'>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">核心内容 <span class="text-danger">*</span></label>
                        <textarea class="form-control" name="principles" rows="8" required
                                  placeholder="知识条目的核心内容，详细阐述原理、要点等..."></textarea>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">常见误区</label>
                        <textarea class="form-control" name="pitfalls" rows="3"
                                  placeholder="常见的理解误区或易错点..."></textarea>
                    </div>
                    <div id="singleMsg" class="alert d-none"></div>
                    <button type="submit" class="btn btn-primary">
                        <i class="bi bi-check-lg"></i> 提交
                    </button>
                </form>
            </div>
        </div>
    </div>

    <!-- 批量导入 -->
    <div class="tab-pane fade" id="batch">
        <div class="card">
            <div class="card-body">
                <div class="mb-3">
                    <label class="form-label">选择 JSON 文件</label>
                    <input type="file" class="form-control" id="jsonFile" accept=".json">
                </div>
                <p class="text-muted small">JSON 文件格式：一个数组，每个元素含 subject, category, difficulty, tags, principles, pitfalls 字段。</p>
                <pre class="bg-light p-3 rounded small">[
  {
    "subject": "什么是依赖注入？",
    "category": "Spring",
    "difficulty": "中等",
    "tags": "[\"Spring\",\"DI\"]",
    "principles": "依赖注入是...",
    "pitfalls": "注意循环依赖..."
  }
]</pre>
                <div id="batchMsg" class="alert d-none"></div>
                <button type="button" class="btn btn-success" onclick="batchImport()">
                    <i class="bi bi-cloud-upload"></i> 导入
                </button>
            </div>
        </div>
    </div>
</div>

<script>
// Load categories
$(function() {
    var categories = ${categoriesJson};
    var html = '<option value="">请选择分类</option>';
    categories.forEach(function(c) {
        html += '<option value="' + c + '">' + c + '</option>';
    });
    $('#categorySelect').html(html);
});

// Single form submit
$('#singleForm').on('submit', function(e) {
    e.preventDefault();
    var data = {
        subject: $('[name=subject]').val(),
        category: $('[name=category]').val(),
        difficulty: $('[name=difficulty]').val(),
        tags: $('[name=tags]').val(),
        principles: $('[name=principles]').val(),
        pitfalls: $('[name=pitfalls]').val()
    };
    $.ajax({
        url: '/api/knowledge/add',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(data),
        success: function(res) {
            if (res.code === 200) {
                $('#singleMsg').removeClass('d-none alert-danger').addClass('alert-success').text('添加成功！');
                $('#singleForm')[0].reset();
            } else {
                $('#singleMsg').removeClass('d-none alert-success').addClass('alert-danger').text(res.message);
            }
        }
    });
});

// Batch import
function batchImport() {
    var file = $('#jsonFile')[0].files[0];
    if (!file) {
        $('#batchMsg').removeClass('d-none alert-success').addClass('alert-danger').text('请选择文件');
        return;
    }
    var formData = new FormData();
    formData.append('file', file);
    $('#batchMsg').removeClass('d-none alert-danger alert-success').addClass('alert-info').text('导入中...');
    $.ajax({
        url: '/api/knowledge/batch-import',
        type: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        success: function(res) {
            if (res.code === 200) {
                $('#batchMsg').removeClass('d-none alert-info alert-danger').addClass('alert-success').text(res.data || res.message);
            } else {
                $('#batchMsg').removeClass('d-none alert-info alert-success').addClass('alert-danger').text(res.message);
            }
        }
    });
}
</script>

<%@ include file="footer.jsp" %>
