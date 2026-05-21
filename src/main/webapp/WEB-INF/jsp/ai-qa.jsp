<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="header.jsp" %>

<h3><i class="bi bi-robot"></i> AI 智能问答</h3>
<p class="text-muted">输入技术问题，AI 将基于知识库内容为您生成回答</p>

<div class="row">
    <div class="col-md-8">
        <div class="card">
            <div class="card-body">
                <div class="mb-3">
                    <label class="form-label fw-bold">输入您的问题</label>
                    <textarea class="form-control" id="question" rows="3"
                              placeholder="例如：什么是依赖注入？请解释 Spring Boot 的自动配置原理..."></textarea>
                </div>
                <button class="btn btn-primary" id="askBtn" onclick="ask()">
                    <i class="bi bi-send"></i> 提问
                </button>
                <button class="btn btn-outline-secondary" onclick="clearChat()">清空</button>
                <div class="spinner-border spinner-border-sm text-primary d-none ms-2" id="loading" role="status"></div>
            </div>
        </div>

        <div id="answerArea" class="mt-3"></div>
    </div>

    <div class="col-md-4">
        <div class="card">
            <div class="card-header">使用说明</div>
            <div class="card-body">
                <ul class="small">
                    <li>系统会先在知识库中检索相关内容</li>
                    <li>基于检索结果，由 AI 生成回答</li>
                    <li>目前基于 DeepSeek Chat 模型</li>
                    <li>答案仅供参考，请以实际为准</li>
                </ul>
            </div>
        </div>
    </div>
</div>

<script>
function ask() {
    var question = $('#question').val().trim();
    if (!question) return;

    $('#askBtn').prop('disabled', true);
    $('#loading').removeClass('d-none');
    $('#answerArea').prepend('<div class="card mb-3 border-primary"><div class="card-header"><strong>您的问题：</strong>' + escapeHtml(question) + '</div></div>');

    $.ajax({
        url: '/api/ai/ask',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ question: question }),
        success: function(res) {
            var answerHtml = '<div class="card mb-3 border-success">'
                + '<div class="card-header bg-success text-white"><strong>AI 回答</strong></div>'
                + '<div class="card-body"><pre style="white-space: pre-wrap; margin: 0;">' + escapeHtml(res.data || res.message) + '</pre></div>'
                + '</div>';
            $('#answerArea').prepend(answerHtml);
        },
        error: function() {
            $('#answerArea').prepend('<div class="alert alert-danger">请求失败，请检查 AI 服务配置</div>');
        },
        complete: function() {
            $('#askBtn').prop('disabled', false);
            $('#loading').addClass('d-none');
            $('#question').val('');
        }
    });
}

function clearChat() { $('#answerArea').html(''); }

function escapeHtml(text) {
    if (!text) return '';
    return text.replace(/</g, '&lt;').replace(/>/g, '&gt;');
}
</script>

<%@ include file="footer.jsp" %>
