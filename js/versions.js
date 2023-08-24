(function() {
  const sort_key = function(name) {
    const segments = name.split("\.");
    const sorts = [];
    segments.forEach(segment => {
      const parts = segment.match(/^(\d+)([^\d]?)$/);
      if (parts) {
        sorts.push(parts[1].padStart(3, '0') + parts[2]);
      } else {
        sorts.push(segment);
      }
    });
    return sorts.join(".");
  };

  const display_versions = function(ghpages) {
    const vermap = {};
    ghpages.forEach(entry => {
      if (entry.type === "dir" && entry.name.match(/^\d/)) {
        const sortKey = sort_key(entry.name);
        vermap[sortKey] = entry.name;
      }
    });
    const verkeys = Object.keys(vermap);
    verkeys.sort().reverse();

    const versions = [];
    verkeys.forEach(key => {
      versions.push(vermap[key]);
    });

    if (versions.length == 1) {
      window.location = "/" + versions[0];
    }

    const ul = document.querySelector("#versions");
    versions.forEach(version => {
      const li = document.createElement("LI");
      const a = document.createElement("A");
      a.innerHTML = version;
      a.setAttribute("href", "/" + version);
      li.appendChild(a);
      ul.appendChild(li);
    });
  };

  display_errors = function(errors) {
    const div = document.querySelector("#versions");
    div.innerHTML = errors.message;
    div.setAttribute("style", "color:red;");
  }

  fetch("https://api.github.com/repos/nineml/nineml/contents/?ref=gh-pages")
    .then(response => {
      if (response.status === 200) {
        response.json()
          .then(data => { display_versions(data) })
      } else {
        display_errors({"message": `Failed to read data: ${response.statusText}`});
      }
    })
    .catch(err => { display_errors(err); });
})();

