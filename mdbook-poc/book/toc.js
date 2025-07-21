// Populate the sidebar
//
// This is a script, and not included directly in the page, to control the total size of the book.
// The TOC contains an entry for each page, so if each page includes a copy of the TOC,
// the total size of the page becomes O(n**2).
class MDBookSidebarScrollbox extends HTMLElement {
    constructor() {
        super();
    }
    connectedCallback() {
        this.innerHTML = '<ol class="chapter"><li class="chapter-item expanded affix "><a href="index.html">Welcome</a></li><li class="chapter-item expanded affix "><li class="spacer"></li><li class="chapter-item expanded affix "><li class="part-title">Our Purpose</li><li class="chapter-item expanded "><a href="Our-Purpose/manifesto.html">📜 Manifesto</a></li><li class="chapter-item expanded "><a href="Our-Purpose/evolution.html">🔄 The Evolution Of Technology</a></li><li class="chapter-item expanded "><a href="Our-Purpose/self-sov-alternative.html">🔓 A Self-Sovereign Alternative</a></li><li class="chapter-item expanded "><a href="Our-Purpose/long-term.html">🔭 The Long-Term Goal</a></li><li class="chapter-item expanded "><a href="Our-Purpose/creation-brand.html">🎨 Creation of the SELF Brand</a></li><li class="chapter-item expanded "><a href="Our-Purpose/Media-Coverage.html">🔈 Media Coverage</a></li><li class="chapter-item expanded affix "><li class="spacer"></li><li class="chapter-item expanded affix "><li class="part-title">Roadmap</li><li class="chapter-item expanded "><a href="Roadmap/Introduction.html">📖 Introduction</a></li><li class="chapter-item expanded "><a href="Roadmap/Beta-Web-App.html">🧪 Beta Web App</a></li><li class="chapter-item expanded "><a href="Roadmap/Super-App-Testnet.html">📱 Super-App &amp; Testnet</a></li><li class="chapter-item expanded "><div>💎 Token</div><a class="toggle"><div>❱</div></a></li><li><ol class="section"><li class="chapter-item "><a href="Roadmap/Token/Overview.html">🌟 Overview</a></li><li class="chapter-item "><a href="Roadmap/Token/Smart-Contract-Architecture.html">🔗 Smart Contract Architecture</a></li><li class="chapter-item "><a href="Roadmap/Token/Tokenomics.html">📊 Tokenomics</a></li></ol></li><li class="chapter-item expanded "><a href="Roadmap/SDK.html">🛠️ SDK</a></li><li class="chapter-item expanded "><a href="Roadmap/Developer-Incentives.html">🎁 Developer Incentives</a></li><li class="chapter-item expanded "><a href="Roadmap/SELF-OS.html">💻 SELF Operating System</a></li><li class="chapter-item expanded affix "><li class="spacer"></li><li class="chapter-item expanded affix "><li class="part-title">Technical Documentation</li><li class="chapter-item expanded "><a href="Technical-Docs/SELF-Chain/SELF_Chain_Architecture.html">Architecture</a></li><li class="chapter-item expanded "><a href="Technical-Docs/SELF-Chain/Why-Build-A-Blockchain.html">Why Build A Blockchain</a></li><li class="chapter-item expanded "><a href="Technical-Docs/PoAI/Proof-of-AI.html">Overview</a></li><li class="chapter-item expanded "><a href="Technical-Docs/PoAI/Taxonomy.html">Taxonomy</a></li><li class="chapter-item expanded "><a href="Technical-Docs/PoAI/AI-Block-Builder-Algorithm.html">AI Block Builder Algorithm</a></li><li class="chapter-item expanded "><a href="Technical-Docs/PoAI/Voting-Algorithm.html">Voting Algorithm</a></li><li class="chapter-item expanded "><a href="Technical-Docs/PoAI/AI-Validator-Algorithm.html">AI-Validator Algorithm</a></li><li class="chapter-item expanded "><a href="Technical-Docs/PoAI/Color-Marker-System.html">Color Marker System</a></li><li class="chapter-item expanded "><a href="Technical-Docs/PoAI/Color-Marker-Examples.html">Color Marker Examples</a></li><li class="chapter-item expanded "><a href="Technical-Docs/PoAI/Validation-Process.html">Validation Process</a></li><li class="chapter-item expanded "><a href="Technical-Docs/PoAI/The-Incentive.html">The Incentive</a></li><li class="chapter-item expanded "><a href="Technical-Docs/PoAI/Governance_Implementation.html">Governance Implementation</a></li><li class="chapter-item expanded "><a href="Technical-Docs/Cloud-Architecture/Overview.html">Overview</a></li><li class="chapter-item expanded "><a href="Technical-Docs/Cloud-Architecture/Developer-Integration.html">Developer Integration</a></li><li class="chapter-item expanded "><a href="Technical-Docs/Cloud-Architecture/Running-Your-Own-Node.html">Running Your Own Node</a></li><li class="chapter-item expanded "><a href="Technical-Docs/Constellation/Overview.html">Overview</a></li><li class="chapter-item expanded "><a href="Technical-Docs/Constellation/Industry_Validation_Rules.html">Industry Validation Rules</a></li><li class="chapter-item expanded "><a href="Technical-Docs/Developer-Resources/Getting_Started_Testnet.html">Getting Started - Testnet</a></li><li class="chapter-item expanded "><a href="Technical-Docs/Developer-Resources/PUBLIC_INTERFACES.html">Public Interfaces</a></li><li class="chapter-item expanded "><a href="Technical-Docs/Grid-Compute/Future-Vision.html">Future Vision</a></li><li class="chapter-item expanded "><a href="Technical-Docs/Security/Overview.html">Overview</a></li><li class="chapter-item expanded "><a href="Technical-Docs/Security/Post_Quantum_Cryptography.html">Post Quantum Cryptography</a></li><li class="chapter-item expanded "><a href="Technical-Docs/Security/AI_Capacity_Implementation.html">AI Capacity Implementation</a></li><li class="chapter-item expanded "><a href="Technical-Docs/Security/Pattern_Analysis_Security.html">Pattern Analysis Security</a></li><li class="chapter-item expanded "><a href="Technical-Docs/Storage/Hybrid_Architecture.html">Hybrid Architecture</a></li><li class="chapter-item expanded "><a href="Technical-Docs/Storage/Storage_Integration.html">Storage Integration</a></li><li class="chapter-item expanded "><a href="Technical-Docs/Integration/Rosetta-API-Integration.html">Rosetta API Integration</a></li><li class="chapter-item expanded "><a href="Technical-Docs/Integration/Minima-Integration.html">Minima Integration</a></li><li class="chapter-item expanded "><a href="Technical-Docs/Performance/Advanced_TPS_Optimization.html">Advanced TPS Optimization</a></li><li class="chapter-item expanded "><a href="Technical-Docs/Validate/index.html">Validator Guide</a></li></ol>';
        // Set the current, active page, and reveal it if it's hidden
        let current_page = document.location.href.toString().split("#")[0].split("?")[0];
        if (current_page.endsWith("/")) {
            current_page += "index.html";
        }
        var links = Array.prototype.slice.call(this.querySelectorAll("a"));
        var l = links.length;
        for (var i = 0; i < l; ++i) {
            var link = links[i];
            var href = link.getAttribute("href");
            if (href && !href.startsWith("#") && !/^(?:[a-z+]+:)?\/\//.test(href)) {
                link.href = path_to_root + href;
            }
            // The "index" page is supposed to alias the first chapter in the book.
            if (link.href === current_page || (i === 0 && path_to_root === "" && current_page.endsWith("/index.html"))) {
                link.classList.add("active");
                var parent = link.parentElement;
                if (parent && parent.classList.contains("chapter-item")) {
                    parent.classList.add("expanded");
                }
                while (parent) {
                    if (parent.tagName === "LI" && parent.previousElementSibling) {
                        if (parent.previousElementSibling.classList.contains("chapter-item")) {
                            parent.previousElementSibling.classList.add("expanded");
                        }
                    }
                    parent = parent.parentElement;
                }
            }
        }
        // Track and set sidebar scroll position
        this.addEventListener('click', function(e) {
            if (e.target.tagName === 'A') {
                sessionStorage.setItem('sidebar-scroll', this.scrollTop);
            }
        }, { passive: true });
        var sidebarScrollTop = sessionStorage.getItem('sidebar-scroll');
        sessionStorage.removeItem('sidebar-scroll');
        if (sidebarScrollTop) {
            // preserve sidebar scroll position when navigating via links within sidebar
            this.scrollTop = sidebarScrollTop;
        } else {
            // scroll sidebar to current active section when navigating via "next/previous chapter" buttons
            var activeSection = document.querySelector('#sidebar .active');
            if (activeSection) {
                activeSection.scrollIntoView({ block: 'center' });
            }
        }
        // Toggle buttons
        var sidebarAnchorToggles = document.querySelectorAll('#sidebar a.toggle');
        function toggleSection(ev) {
            ev.currentTarget.parentElement.classList.toggle('expanded');
        }
        Array.from(sidebarAnchorToggles).forEach(function (el) {
            el.addEventListener('click', toggleSection);
        });
    }
}
window.customElements.define("mdbook-sidebar-scrollbox", MDBookSidebarScrollbox);
