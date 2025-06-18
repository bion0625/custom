const PublicAdmin: React.FC = () => {
    return (
        <div className="min-h-screen bg-gray-50 p-4 sm:p-6">
            <div className="max-w-6xl mx-auto flex flex-col md:flex-row gap-6">
                {/*사이드바*/}
                <aside className="w-full md:w-1/4 border-b md:border-b-0 md:border-r bg-gray-100 p-4 rounded-md md:rounded-none overflow-auto h-auto md:h-[80vh]">
                    <button className="mb-2 w-full py-2 bg-indigo-500 text-white rounded-lg hover:bg-indigo-600 transition">돌아가기</button>
                    <button className="mb-4 w-full py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition">로그아웃</button>
                    <button className="mb-4 w-full py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition">+ 새 장면</button>
                    <ul className="space-y-2">
                        <li>
                            <button className={"w-full text-left px-2 py-1 rounded bg-indigo-200 font-semibold"}>장면 선택 버튼(선택)</button>
                            <button className={"w-full text-left px-2 py-1 rounded hover:bg-gray-200"}>장면 선택 버튼(미선택)</button>
                        </li>
                    </ul>
                </aside>
                {/*에디터 폼*/}
                <section className="w-full md:flex-1 bg-white p-4 rounded-md shadow flex flex-col space-y-4 overflow-auto h-auto md:h-[80vh]">
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-4">
                        <div>
                            <label className="block text-sm font-medium">화자</label>
                            <input type="text" className="mt-1 w-full border rounded p-2"/>
                        </div>
                        <div>
                            <label className="block text-sm font-medium">배경이미지</label>
                            <input type="text" className="mt-1 w-full border rounded p-2"/>
                        </div>
                    </div>

                    {/*TITLE TEXT*/}
                    <div className="mb-4">
                        <label className="block text-sm font-medium">text</label>
                        <textarea className="mt-1 w-full border rounded p-2 h-32">text content</textarea>
                    </div>

                    {/*CHOICE TEXT*/}
                    <div className="mb-4">
                        <div className="flex justify-between items-center mb-2">
                            <label className="text-sm font-medium">Choice</label>
                            <button className="text-sm text-green-600">+ 추가</button>
                        </div>
                        <div className="space-y-2">
                            <div className="flex flex-col sm:flex-row sm:space-x-2 space-y-2 sm:space-y-0">
                                <input type="text" placeholder="답변 텍스트" className="border rounded p-1 flex-1"/>
                                <input type="text" placeholder="다음 장면 title text with id" className="border rounded p-1 flex-1"/>
                                <button className="text-red-600 self-center">x(삭제 버튼)</button>
                            </div>
                        </div>
                    </div>

                    {/*FLAG 스타트(하나만) or 엔드*/}
                    <div className="flex flex-col sm:flex-row sm:space-x-4 mb-4">
                        <label className="inline-flex items-center space-x-2">
                        {/*비활성화시*/}
                        {/*<label className="inline-flex items-center space-x-2 opacity-50 pointer-events-none">*/}
                            <input type="checkbox" className="form-checkbox"/>
                            <span>Start Scene</span>
                        </label>
                        <label className="inline-flex items-center space-x-2">
                            <input type="checkbox" className="form-checkbox"/>
                            <span>End Scene</span>
                        </label>
                    </div>

                    {/*버튼*/}
                    <div className="flex flex-col sm:flex-row sm:space-x-2 space-y-2 sm:space-y-0">
                        <button className="w-full sm:w-auto px-4 py-2 bg-blue-600 text-white rounded-lg">저장</button>
                        <button className="w-full sm:w-auto px-4 py-2 bg-red-600 text-white rounded-lg">삭제</button>
                    </div>
                </section>
            </div>
        </div>
    )
}

export default PublicAdmin;