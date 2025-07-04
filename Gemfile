source "https://rubygems.org"

# Jekyll version compatible with GitHub Pages
gem "github-pages", group: :jekyll_plugins

# Just the Docs theme
gem "just-the-docs"

# GitHub Pages plugins
group :jekyll_plugins do
  gem "jekyll-seo-tag"
  gem "jekyll-sitemap"
  gem "jekyll-github-metadata"
  gem "jekyll-include-cache"
  gem "jekyll-feed"
  gem "jekyll-paginate"
  gem "jekyll-redirect-from"
  gem "jemoji"
end

# Windows and JRuby does not include zoneinfo files, so bundle the tzinfo-data gem
platforms :mingw, :x64_mingw, :mswin, :jruby do
  gem "tzinfo", ">= 1", "< 3"
  gem "tzinfo-data"
end

# Performance-booster for watching directories on Windows
gem "wdm", "~> 0.1", :platforms => [:mingw, :x64_mingw, :mswin]

# Lock `http_parser.rb` gem to `v0.6.x` on JRuby builds
gem "http_parser.rb", "~> 0.6.0", :platforms => [:jruby]

# For serving the site locally
gem "webrick", "~> 1.8"