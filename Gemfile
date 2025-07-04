source "https://rubygems.org"

# Jekyll
gem "jekyll", "~> 4.3"

# Theme
gem "jekyll-theme-hydejack", "~> 9.1"

# Plugins
group :jekyll_plugins do
  gem "jekyll-feed"
  gem "jekyll-seo-tag"
  gem "jekyll-sitemap"
  gem "jekyll-paginate"
  gem "jekyll-include-cache"
  gem "jekyll-redirect-from"
  gem "jekyll-relative-links"
  gem "jekyll-default-layout"
  gem "jekyll-titles-from-headings"
  gem "jekyll-optional-front-matter"
  gem "jekyll-readme-index"
end

# Windows and JRuby
platforms :mingw, :x64_mingw, :mswin, :jruby do
  gem "tzinfo", ">= 1", "< 3"
  gem "tzinfo-data"
end

# Performance-booster for watching directories on Windows
gem "wdm", "~> 0.1", :platforms => [:mingw, :x64_mingw, :mswin]

# HTTP server
gem "webrick", "~> 1.8"

# Additional dependencies
gem "faraday-retry"