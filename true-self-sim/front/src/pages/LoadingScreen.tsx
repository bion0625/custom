const LoadingScreen = () => {
  return (
      <div
          className="fixed inset-0 bg-cover bg-center flex items-center justify-center"
          style={{
              backgroundImage: `url('/background/loading-background.png')`,
          }}
      >
      </div>
  );
};
export default LoadingScreen;