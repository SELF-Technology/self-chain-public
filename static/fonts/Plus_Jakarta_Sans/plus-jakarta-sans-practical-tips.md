### Practical tips for smooth usage — Plus Jakarta Sans

- **Get the right files**: Use the variable WOFF2 (wght 200–800; ital if needed) for optimal performance.
- **Self‑host for Tauri/offline**: Bundle WOFF2 in your app; prefer variable over multiple static weights.
- **Keep payload small**: Limit to 1–2 weights (e.g., 400/600) or a narrow variable range; subset to Latin if that's all you need.
- **Preload + swap**: Preload your main face and set `font-display: swap` to avoid FOIT.
- **Fallback stack**: `"Plus Jakarta Sans", Inter, system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif`.
- **UI tuning**: Body 15–16px with `line-height: 1.5–1.6`; headings `line-height: 1.2–1.3`. Add `letter-spacing: 0.005–0.02em` for small sizes; slight negative tracking for big display (-0.005–0.01em).
- **Numbers in data**: If available, use `font-variant-numeric: tabular-nums` for aligned digits.
- **Docs parity**: In Google Docs, add "Plus Jakarta Sans" via "More fonts," then set as default for Normal text/Headings.

**Plus Jakarta Sans Characteristics:**
- **Personality**: Modern, friendly, professional with subtle geometric touches
- **Best for**: Tech companies, modern brands, user interfaces
- **Weight range**: 200 (ExtraLight) to 800 (ExtraBold)
- **Recommended weights**: 400 (Regular), 500 (Medium), 600 (SemiBold), 700 (Bold)
- **Special features**: Excellent readability, great for both UI and branding

**Optimal Usage:**
- **Headings**: 600 (SemiBold) or 700 (Bold) for impact
- **Body text**: 400 (Regular) or 500 (Medium) for readability
- **UI elements**: 500 (Medium) for buttons, 600 (SemiBold) for labels
- **Display text**: 700 (Bold) or 800 (ExtraBold) for hero sections

Example CSS (variable font):
```css
/* Variable font implementation */
@font-face {
	font-family: "Plus Jakarta Sans";
	src: local("Plus Jakarta Sans"),
	     url("/fonts/Plus_Jakarta_Sans/PlusJakartaSans-VariableFont_wght.ttf") format("truetype-variations");
	font-weight: 200 800;
	font-style: normal;
	font-display: swap;
}
@font-face {
	font-family: "Plus Jakarta Sans";
	src: local("Plus Jakarta Sans Italic"),
	     url("/fonts/Plus_Jakarta_Sans/PlusJakartaSans-Italic-VariableFont_wght.ttf") format("truetype-variations");
	font-weight: 200 800;
	font-style: italic;
	font-display: swap;
}

/* Usage */
:root { 
	--font-sans: "Plus Jakarta Sans", Inter, system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif; 
}
body { 
	font-family: var(--font-sans); 
	font-weight: 400; 
	line-height: 1.6;
}
h1, h2, h3 { 
	font-weight: 600; 
	line-height: 1.2;
	letter-spacing: -0.01em;
}
h4, h5, h6 { 
	font-weight: 500; 
	line-height: 1.3;
}
```

HTML preload:
```html
<link rel="preload" as="font" type="font/ttf" href="/fonts/Plus_Jakarta_Sans/PlusJakartaSans-VariableFont_wght.ttf" crossorigin="anonymous">
```

**Performance Tips:**
- **Variable font**: Use the variable font file for better performance
- **Subsetting**: Use a tool (e.g., glyph subsetter) to create `latin`/`latin-ext` subsets if needed
- **Critical weights**: Preload only the weights you actually use (400, 600 for most sites)
- **Display swap**: Always use `font-display: swap` for better perceived performance

**Google Docs Integration:**
1. Open Google Docs
2. Go to Format > Font > More fonts
3. Search for "Plus Jakarta Sans"
4. Add it to your fonts
5. Set as default for Normal text and Headings
6. Use weights: Regular (400), Medium (500), SemiBold (600), Bold (700)

**Brand Consistency:**
- Use consistent weight hierarchy across all materials
- Maintain the same fallback stack everywhere
- Consider creating a brand style guide with specific font usage rules
- Test readability across different screen sizes and resolutions
