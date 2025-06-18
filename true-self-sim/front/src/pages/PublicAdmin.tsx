const PublicAdmin: React.FC = () => {
    return (
        <div>
            <div>
                {/*사이트바*/}
                <aside>
                    <button>돌아가기</button>
                    <button>로그아웃</button>
                    <button>+ 새 장면</button>
                    <ul>
                        <li>
                            <button>장면 선택 버튼</button>
                        </li>
                    </ul>
                </aside>
                {/*에디터 폼*/}
                <section>
                    <div>
                        <div>
                            <label>화자</label>
                            <input/>
                        </div>
                        <div>
                            <label>배경이미지</label>
                            <input/>
                        </div>
                    </div>

                    {/*TITLE TEXT*/}
                    <div>
                        <label>text</label>
                        <textarea>text content</textarea>
                    </div>

                    {/*CHOICE TEXT*/}
                    <div>
                        <div>
                            <label>Choice</label>
                            <button>+ 추가</button>
                        </div>
                        <div>
                            <div>
                                <input placeholder="답변 텍스트"/>
                                <input placeholder="다음 씬 title text with id"/>
                                <button>x(삭제 버튼)</button>
                            </div>
                        </div>
                    </div>

                    {/*FLAG 스타트(하나만) or 엔드*/}
                    <div>
                        <label>
                            <input type="checkbox"/>
                            <span>Start Scene</span>
                        </label>
                        <label>
                            <input type="checkbox"/>
                            <span>End Scene</span>
                        </label>
                    </div>

                    {/*버튼*/}
                    <div>
                        <button>저장</button>
                        <button>삭제</button>
                    </div>
                </section>
            </div>
        </div>
    )
}

export default PublicAdmin;