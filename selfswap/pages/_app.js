function MyApp({ Component, pageProps }) {
  return (
    <div className="min-h-screen bg-white">
      <Component {...pageProps} />
    </div>
  );
}

export default MyApp;
