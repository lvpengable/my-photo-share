document.addEventListener('DOMContentLoaded', function() {
    const uploadForm = document.getElementById('uploadForm');
    const photoInput = document.getElementById('photoInput');
    const description = document.getElementById('description');
    const uploadMessage = document.getElementById('uploadMessage');
    const photosContainer = document.getElementById('photosContainer');

    // 全局函数定义
    window.openModal = function(imageSrc, imageName) {
        const modal = document.createElement('div');
        modal.className = 'modal';
        modal.style.display = 'block'; // 覆盖CSS中的display: none;
        modal.onclick = function() {
            document.body.removeChild(modal);
        };

        const modalContent = document.createElement('div');
        modalContent.className = 'modal-content';

        const img = document.createElement('img');
        img.src = imageSrc;
        img.alt = imageName;

        const closeBtn = document.createElement('span');
        closeBtn.className = 'close';
        closeBtn.innerHTML = '&times;';
        closeBtn.onclick = function() {
            document.body.removeChild(modal);
        };

        modalContent.appendChild(img);
        modalContent.appendChild(closeBtn);
        modal.appendChild(modalContent);
        document.body.appendChild(modal);

        // ESC键关闭
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                document.body.removeChild(modal);
            }
        });
    };

    // 获取或生成并保存 deviceId
    function getOrCreateDeviceId() {
        let deviceId = localStorage.getItem('photo_share_device_id');
        if (!deviceId) {
            // 生成一个随机的 UUID v4（标准设备唯一标识格式）
            deviceId = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
                const r = Math.random() * 16 | 0;
                const v = c === 'x' ? r : (r & 0x3 | 0x8);
                return v.toString(16);
            });
            // 存储到 localStorage，长期保存
            localStorage.setItem('photo_share_device_id', deviceId);
        }
        return deviceId;
    }

// 在 DOM 加载时获取 deviceId（也可以在全局变量中保存）
    const currentDeviceId = getOrCreateDeviceId();

    // 点赞照片
    window.likePhoto = function(photoId, button) {
        const ipAddress = 'client-ip'; // 在实际应用中，应该从后端获取或传递

        fetch(`/like/${photoId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                likerDeviceId: currentDeviceId,
            })

        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    button.classList.add('liked');
                    button.innerHTML = '<span>❤️</span><span>已点赞</span>';
                    // 重新加载照片以更新点赞数
                    loadPhotosViaAjax();
                } else {
                    alert('点赞失败，请重试');
                }
            })
            .catch(error => {
                console.error('Error liking photo:', error);
                alert('点赞失败，请重试');
            });
    }

    // 加载照片
    loadPhotos();

    // 上传表单提交
    uploadForm.addEventListener('submit', function(e) {
        e.preventDefault();

        const formData = new FormData();
        const photoFile = photoInput.files[0];

        if (!photoFile) {
            showMessage('请选择一张照片', 'error');
            return;
        }

        formData.append('photo', photoFile);
        formData.append('description', description.value);

        // 显示上传中状态
        const uploadBtn = document.querySelector('.upload-btn');
        const originalText = uploadBtn.textContent;
        uploadBtn.disabled = true;
        uploadBtn.textContent = '上传中...';

        fetch('/upload', {
            method: 'POST',
            body: formData
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    showMessage(data.message, 'success');
                    uploadForm.reset();
                    loadPhotos(); // 重新加载照片列表
                } else {
                    showMessage(data.error || '上传失败', 'error');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showMessage('上传失败，请重试', 'error');
            })
            .finally(() => {
                // 恢复按钮状态
                uploadBtn.disabled = false;
                uploadBtn.textContent = originalText;
                setTimeout(() => {
                    hideMessage();
                }, 3000);
            });
    });

    // 加载照片列表
    function loadPhotos() {
        fetch('/')
            .then(response => response.text())
            .then(html => {
                // 这里简化处理，实际项目中建议使用专门的API端点
                // 为了简化，我们重新获取整个页面然后提取照片部分
                // 更好的做法是创建一个专门的API端点返回JSON数据
                loadPhotosViaAjax();
            })
            .catch(error => {
                console.error('Error loading photos:', error);
            });
    }

    // 通过AJAX加载照片
    function loadPhotosViaAjax() {
        fetch('/photos-data')
            .then(response => response.json())
            .then(photos => {
                displayPhotos(photos);
            })
            .catch(error => {
                // 如果专门的API不存在，使用当前页面重新加载
                console.log('Using fallback method to load photos');
                // 简单的重新加载页面方法
                setTimeout(() => {
                    location.reload();
                }, 1000);
            });
    }

    // 显示照片
    function displayPhotos(photos) {
        if (!photos || photos.length === 0) {
            photosContainer.innerHTML = '<p style="text-align: center; color: #666; font-size: 1.2rem;">还没有照片，成为第一个分享的人吧！</p>';
            return;
        }

        const photosHtml = photos.map(photo => `
            <div class="photo-card">
                <img src="/static/photos/${photo.id}" alt="${photo.originalName}" class="photo-image" onclick="openModal('/static/photos/${photo.id}', '${photo.originalName}')">
                <div class="photo-info">
                    <div class="photo-description">${photo.description || '暂无描述'}</div>
                    <div class="photo-meta">
                        <span>📅 ${photo.uploadTime}</span>
                        <span>🆔 #${photo.id.substring(0, 8)}</span>
                    </div>
                    <div class="photo-actions">
                        <button class="like-btn ${photo.likedByUser ? 'liked' : ''}" onclick="likePhoto('${photo.id}', this)">
                            <span>❤️</span>
                            <span>点赞</span>
                        </button>
                        <span class="like-count">${photo.likes}</span>
                    </div>
                </div>
                <!-- ✅ 新增：评论输入区域 -->
                <div class="comment-section">
                    <textarea 
                        placeholder="写下你的评论..." 
                        class="comment-input" 
                        data-photo-id="${photo.id}"
                        rows="3"
                        style="
                            width: 100%;
                            margin-top: 10px;
                            padding: 8px;
                            border: 1px solid #ddd;
                            border-radius: 5px;
                            resize: vertical;
                            font-family: inherit;
                        "
                    ></textarea>
                    <button 
                        class="comment-submit-btn" 
                        data-photo-id="${photo.id}"
                        style="
                            margin-top: 5px;
                            padding: 6px 12px;
                            background: #2196F3;
                            color: white;
                            border: none;
                            border-radius: 5px;
                            cursor: pointer;
                            font-size: 0.9rem;
                        "
                    >
                        发表评论
                    </button>
                </div>
            </div>
            
            <!-- 在每张照片的 HTML 模板中，添加如下区块（放在评论输入框之前或之后）-->
            <div class="comments-list" data-photo-id="${photo.id}">
                <!-- 评论列表将通过 AJAX 加载并插入这里 -->
            </div>
        `).join('');

        // ✅ 第一步：先把照片卡片插入 DOM
        photosContainer.innerHTML = photosHtml;

        // ✅ 第二步：遍历每张照片，加载评论并渲染到对应容器中
        photos.forEach(function(photo) {
            const photoId = photo.id;

            fetch(`/api/comments?photoId=${photoId}`)
                .then(response => response.json())
                .then(res => {
                    if (res.success) {
                        const comments = res.comments;
                        const commentsContainer = document.querySelector(`.comments-list[data-photo-id="${photoId}"]`);

                        if (comments && comments.length > 0) {
                            let commentsHtml = '';
                            for (let i = 0; i < comments.length; i++) {
                                const c = comments[i];
                                commentsHtml += `
                                <div style="margin-bottom: 10px; padding: 8px; background: #f0f0f0; border-radius: 5px; font-size: 0.9rem;">
                                    <strong>${c.commenterName || '匿名用户'}:</strong><br>
                                    ${c.commentText}<br>
                                    <small style="color: #888;">${new Date(c.createdAt).toLocaleString()}</small>
                                </div>
                            `;
                            }
                            commentsContainer.innerHTML = commentsHtml;
                        } else {
                            commentsContainer.innerHTML = '<p style="font-size: 0.9rem; color: #888;">暂无评论，快来抢沙发吧！</p>';
                        }
                    } else {
                        console.error('获取评论失败:', res.message);
                        commentsContainer.innerHTML = '<p style="font-size: 0.9rem; color: #888;">暂无评论</p>';
                    }
                })
                .catch(error => {
                    console.error('加载评论出错:', error);
                    // 可以留空或显示错误提示
                });
        });
    }

    // ✅ 使用事件委托绑定 .comment-submit-btn 的点击（动态生效！）
    photosContainer.addEventListener('click', function (e) {
        if (e.target.classList.contains('comment-submit-btn')) {
            const btn = e.target;
            const photoId = btn.getAttribute('data-photo-id');
            const textarea = document.querySelector('.comment-input[data-photo-id="' + photoId + '"]');
            const commentText = textarea.value.trim();

            if (!commentText) {
                alert('请输入评论内容');
                return;
            }

            // 提交评论到后端
            fetch('/api/comments', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    photoId: photoId,
                    commenterId: currentDeviceId,
                    commenterName: '用户',
                    commentText: commentText
                })
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        alert('评论发表成功！');
                        textarea.value = ''; // 清空输入框

                        // ✅ 成功后加载该照片的评论列表
                        fetch('/api/comments?photoId=' + photoId)
                            .then(function (response) {
                                return response.json();
                            })
                            .then(function (res) {
                                if (res.success) {
                                    var comments = res.comments;
                                    var commentsContainer = document.querySelector('.comments-list[data-photo-id="' + photoId + '"]');
                                    if (comments && comments.length > 0) {
                                        var commentsHtml = '';
                                        for (var i = 0; i < comments.length; i++) {
                                            var c = comments[i];
                                            commentsHtml += '<div style="margin-bottom: 10px; padding: 8px; background: #f0f0f0; border-radius: 5px; font-size: 0.9rem;">' +
                                                '<strong>' + (c.commenterName || '匿名用户') + ':</strong><br>' +
                                                c.commentText + '<br>' +
                                                '<small style="color: #888;">' + new Date(c.createdAt).toLocaleString() + '</small>' +
                                                '</div>';
                                        }
                                        commentsContainer.innerHTML = commentsHtml;
                                    } else {
                                        commentsContainer.innerHTML = '<p style="font-size: 0.9rem; color: #888;">暂无评论，快来抢沙发吧！</p>';
                                    }
                                }
                            })
                            .catch(function (error) {
                                console.error('加载评论出错:', error);
                            });
                    } else {
                        alert(data.message || '评论发表失败，请重试');
                    }
                })
                .catch(function (error) {
                    console.error('评论提交出错:', error);
                    alert('评论发表失败，请重试');
                });
        }
    });

    // 显示消息
    function showMessage(message, type) {
        uploadMessage.textContent = message;
        uploadMessage.className = `message ${type}`;
        uploadMessage.style.display = 'block';
    }

    // 隐藏消息
    function hideMessage() {
        uploadMessage.style.display = 'none';
    }

});