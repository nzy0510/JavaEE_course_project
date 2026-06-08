<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="header.jsp" %>

<h3><i class="bi bi-cloud-upload"></i> 上传文档入库</h3>
<p class="text-muted">系统会调用 MarkItDown 将文档转换为 Markdown，自动识别知识分类，再按标题和长度切分为可检索片段。</p>

<div class="card">
    <div class="card-body">
        <form id="uploadForm">
            <div class="mb-3">
                <label class="form-label">选择文档</label>
                <input type="file" class="form-control" id="documentFile"
                       accept=".pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.html,.htm,.md,.txt" required>
            </div>
            <p class="text-muted small">
                支持 PDF、Word、PPT、Excel、HTML、Markdown 和 TXT。当前单文件大小限制由 Spring 上传配置控制。
            </p>
            <div id="uploadMsg" class="alert d-none"></div>
            <button type="submit" class="btn btn-primary">
                <i class="bi bi-cloud-upload"></i> 上传并导入
            </button>
            <a href="/knowledge-list" class="btn btn-outline-secondary ms-2">查看文档管理</a>
        </form>
    </div>
</div>

<div class="card mt-4">
    <div class="card-header">导入流程</div>
    <div class="card-body">
        <ol class="mb-0">
            <li>后端将上传文件保存到受控临时目录。</li>
            <li>调用配置的 <code>MARKITDOWN_PYTHON</code> 执行 <code>-m markitdown</code>。</li>
            <li>根据文件名关键词识别知识分类，例如 AI 大模型资料会归入“大模型”。</li>
            <li>对 Markdown 做字符归一化、噪声清理和分块。</li>
            <li>文档元信息写入 <code>knowledge_document</code>，切片写入 <code>knowledge_chunk</code>。</li>
        </ol>
    </div>
</div>

<script>
$('#uploadForm').on('submit', function(e) {
    e.preventDefault();
    var file = $('#documentFile')[0].files[0];
    if (!file) {
        showUpload('alert-danger', '请选择文档');
        return;
    }
    var formData = new FormData();
    formData.append('file', file);
    showUpload('alert-info', '正在转换并切分文档，请稍候...');
    $.ajax({
        url: '/api/knowledge/documents/import',
        type: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        success: function(res) {
            if (res.code === 200) {
                var doc = res.data;
                showUpload('alert-success', '导入成功：' + doc.originalFilename + '，分类为“' + (doc.knowledgeCategory || '通用知识') + '”，生成 ' + doc.chunkCount + ' 个切片。');
                $('#uploadForm')[0].reset();
            } else {
                showUpload('alert-danger', res.message);
            }
        },
        error: function() {
            showUpload('alert-danger', '上传或转换失败，请检查 MarkItDown 配置');
        }
    });
});

function showUpload(type, message) {
    $('#uploadMsg').removeClass('d-none alert-info alert-success alert-danger').addClass(type).text(message);
}
</script>

<%@ include file="footer.jsp" %>
