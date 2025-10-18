(function() {
  document.querySelectorAll("h2,h3,h4,h5,h6,dt.synopsis").forEach(hx => {
    const wrapper = hx.tagName === "DT" ? hx.parentNode : hx.parentNode.parentNode;
    const id = wrapper.getAttribute("id");

    if (id) {
      const span = document.createElement("span");
      span.classList.add("_sect");

      const anchor = document.createElement("a");
      anchor.setAttribute("href", "#" + id);
      anchor.innerHTML = "#";

      span.appendChild(anchor);

      hx.insertBefore(span, hx.firstChild);
      hx.classList.add("sectmark");
    }
  });
})();

