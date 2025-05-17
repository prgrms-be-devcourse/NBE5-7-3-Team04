import { getPerformances } from '../api/performance';
import { getImageUrl } from '../utils/image';

export default async function Home() {
  try {
    const { content: performances } = await getPerformances();

    return (
      <main className="container mx-auto px-4 py-8">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {performances.map((performance) => (
            <div key={performance.id} className="border rounded-lg overflow-hidden">
              <img
                src={getImageUrl(performance.fileUrl)}
                alt={performance.title}
                className="w-full h-48 object-cover"
              />
              <div className="p-4">
                <h2 className="text-xl font-bold mb-2">{performance.title}</h2>
                <p className="text-gray-600 mb-2">{performance.venue}</p>
                <p className="text-gray-600 mb-2">
                  {new Date(performance.startDate).toLocaleDateString()} ~{' '}
                  {new Date(performance.endDate).toLocaleDateString()}
                </p>
                <p className="text-lg font-semibold">{performance.price.toLocaleString()}원</p>
              </div>
            </div>
          ))}
        </div>
      </main>
    );
  } catch (error) {
    console.error('Error loading performances:', error);
    return (
      <main className="container mx-auto px-4 py-8">
        <div className="text-center text-red-500">
          공연 목록을 불러오는 중 오류가 발생했습니다.
        </div>
      </main>
    );
  }
} 