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

    // 点赞照片
    window.likePhoto = function(photoId, button) {
        const ipAddress = 'client-ip'; // 在实际应用中，应该从后端获取或传递

        fetch(`/like/${photoId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            }
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
            </div>
        `).join('');

        photosContainer.innerHTML = photosHtml;
    }



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