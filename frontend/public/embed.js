(function() {
  const currentScript = document.currentScript;
  const assistantToken = currentScript ? currentScript.getAttribute('data-assistant-token') : null;
  const baseUrl = currentScript ? new URL(currentScript.src).origin : 'http://localhost:3000';

  if (!assistantToken) {
    console.error('[AI Assistant] Missing data-assistant-token attribute on embed script.');
    return;
  }

  // Floating trigger button
  const button = document.createElement('div');
  button.id = 'ai-assistant-widget-btn';
  button.innerHTML = `
    <div style="
      position: fixed;
      bottom: 24px;
      right: 24px;
      width: 60px;
      height: 60px;
      border-radius: 50%;
      background: linear-gradient(135deg, #2563eb, #1d4ed8);
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 8px 24px rgba(37, 99, 235, 0.35);
      cursor: pointer;
      z-index: 999999;
      font-size: 26px;
      transition: transform 0.2s, box-shadow 0.2s;
    ">
      💬
    </div>
  `;

  // Iframe modal container
  const modal = document.createElement('div');
  modal.id = 'ai-assistant-widget-container';
  modal.style.display = 'none';
  modal.innerHTML = `
    <div style="
      position: fixed;
      bottom: 96px;
      right: 24px;
      width: 420px;
      max-width: calc(100vw - 32px);
      height: 620px;
      max-height: calc(100vh - 120px);
      border-radius: 16px;
      overflow: hidden;
      box-shadow: 0 12px 36px rgba(0,0,0,0.22);
      z-index: 999999;
      border: 1px solid rgba(0,0,0,0.1);
      background: white;
    ">
      <iframe
        src="${baseUrl}/chat/${assistantToken}"
        style="width: 100%; height: 100%; border: none;"
        allow="clipboard-write">
      </iframe>
    </div>
  `;

  document.body.appendChild(button);
  document.body.appendChild(modal);

  let isOpen = false;
  button.addEventListener('click', function() {
    isOpen = !isOpen;
    modal.style.display = isOpen ? 'block' : 'none';
    button.querySelector('div').innerHTML = isOpen ? '✕' : '💬';
    button.querySelector('div').style.transform = isOpen ? 'rotate(90deg)' : 'none';
  });
})();
